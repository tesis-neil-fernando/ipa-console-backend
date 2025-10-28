package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.repository.N8nWorkflowRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowSyncService {

    private final N8nApiService n8nService;
    private final N8nWorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // serializa/parsea JSON

    @Transactional
    public SyncSummary pullAndSave() {
        // Llama al servicio que ya maneja errores y formato uniforme
        ResponseEntity<N8nApiService.ApiResponse<String>> resp = n8nService.getWorkflowsRaw();
        N8nApiService.ApiResponse<String> body = resp.getBody();

        if (body == null || !body.isSuccess() || body.getData() == null) {
            throw new RuntimeException("No se pudo obtener workflows desde n8n: " +
                    (body == null ? "respuesta vacía" : body.getMessage()));
        }

        try {
            JsonNode root = objectMapper.readTree(body.getData()); // body.getData() es el JSON crudo de n8n
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                return new SyncSummary(0, 0, 0);
            }

            // Parseo a entidades
            List<WorkflowEntity> entities = new ArrayList<>();
            for (JsonNode wfNode : data) {
                WorkflowEntity e = new WorkflowEntity();
                e.setId(wfNode.path("id").asText());
                e.setName(wfNode.path("name").asText());
                e.setActive(wfNode.path("active").asBoolean());
                // En n8n viene como isArchived; en la entidad lo guardamos como archived
                e.setArchived(wfNode.path("isArchived").asBoolean());
                e.setRawJson(wfNode.toString());
                entities.add(e);
            }

            // Calculamos creados vs actualizados
            Set<String> incomingIds = entities.stream().map(WorkflowEntity::getId).collect(Collectors.toSet());
            Set<String> existingIds = new HashSet<>();
            workflowRepository.findAllById(incomingIds).forEach(w -> existingIds.add(w.getId()));

            int toUpdate = (int) entities.stream().filter(w -> existingIds.contains(w.getId())).count();
            int toCreate = entities.size() - toUpdate;

            // Upsert simple
            workflowRepository.saveAll(entities);

            return new SyncSummary(entities.size(), toCreate, toUpdate);

        } catch (Exception e) {
            throw new RuntimeException("Error procesando workflows", e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class SyncSummary {
        private int total;
        private int created;
        private int updated;
    }
}
