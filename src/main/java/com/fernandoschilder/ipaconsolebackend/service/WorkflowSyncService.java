package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.model.TagEntity;
import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.repository.WorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.repository.TagRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowSyncService {

    private final N8nApiService n8nService;
    private final WorkflowRepository workflowRepository;
    private final TagRepository tagRepository;
    private final ProcessService processService; // <-- NUEVO
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public SyncSummary pullAndSave() {
        ResponseEntity<N8nApiService.ApiResponse<String>> resp = n8nService.getWorkflowsRaw();
        N8nApiService.ApiResponse<String> body = resp.getBody();

        if (body == null || !body.isSuccess() || body.getData() == null) {
            throw new RuntimeException("No se pudo obtener workflows desde n8n: " + (body == null ? "respuesta vacía" : body.getMessage()));
        }

        try {
            JsonNode root = objectMapper.readTree(body.getData());
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                return new SyncSummary(0, 0, 0);
            }

            // 1) Reunir TODAS las tags entrantes
            Map<String, TagEntity> incomingTagsMap = new HashMap<>();
            for (JsonNode wfNode : data) {
                JsonNode tagsNode = wfNode.path("tags");
                if (tagsNode.isArray()) {
                    for (JsonNode t : tagsNode) {
                        String tagId = t.path("id").asText(null);
                        if (tagId == null) continue;
                        TagEntity tag = incomingTagsMap.computeIfAbsent(tagId, id -> new TagEntity());
                        tag.setId(tagId);
                        tag.setName(t.path("name").asText(null));
                        tag.setCreatedAt(parseDate(t.path("createdAt").asText(null)));
                        tag.setUpdatedAt(parseDate(t.path("updatedAt").asText(null)));
                    }
                }
            }

            // 2) Upsert de tags
            if (!incomingTagsMap.isEmpty()) {
                List<TagEntity> existing = tagRepository.findAllById(incomingTagsMap.keySet());
                for (TagEntity ex : existing) {
                    TagEntity in = incomingTagsMap.get(ex.getId());
                    if (in != null) {
                        ex.setName(in.getName());
                        ex.setCreatedAt(in.getCreatedAt());
                        ex.setUpdatedAt(in.getUpdatedAt());
                        incomingTagsMap.put(ex.getId(), ex); // asegurar instancia gestionada
                    }
                }
                List<TagEntity> toCreate = incomingTagsMap.values().stream().filter(t -> existing.stream().noneMatch(e -> e.getId().equals(t.getId()))).toList();
                if (!toCreate.isEmpty()) tagRepository.saveAll(toCreate);
            }

            // 3) Construir workflows y asignar sus tags
            List<WorkflowEntity> entities = new ArrayList<>();
            for (JsonNode wfNode : data) {
                WorkflowEntity e = new WorkflowEntity();
                e.setId(wfNode.path("id").asText());
                e.setName(wfNode.path("name").asText());
                e.setActive(wfNode.path("active").asBoolean());
                e.setArchived(wfNode.path("isArchived").asBoolean());
                e.setRawJson(wfNode.toString());

                Set<TagEntity> tagSet = new HashSet<>();
                JsonNode tagsNode = wfNode.path("tags");
                if (tagsNode.isArray()) {
                    for (JsonNode t : tagsNode) {
                        String tagId = t.path("id").asText(null);
                        if (tagId == null) continue;
                        TagEntity managed = tagRepository.findById(tagId).orElseGet(() -> incomingTagsMap.get(tagId));
                        if (managed != null) tagSet.add(managed);
                    }
                }
                e.setTags(tagSet);

                entities.add(e);
            }

            // 4) Calcular creados/actualizados y guardar workflows
            Set<String> incomingIds = entities.stream().map(WorkflowEntity::getId).collect(Collectors.toSet());
            Set<String> existingIds = new HashSet<>();
            workflowRepository.findAllById(incomingIds).forEach(w -> existingIds.add(w.getId()));

            int toUpdate = (int) entities.stream().filter(w -> existingIds.contains(w.getId())).count();
            int toCreate = entities.size() - toUpdate;

            workflowRepository.saveAll(entities);
            workflowRepository.flush();

            if (toCreate > 0) {
                String nowIso = OffsetDateTime.now().toString();
                List<String> newWorkflowIds = entities.stream()
                        .map(WorkflowEntity::getId)
                        .filter(id -> !existingIds.contains(id))
                        .toList();

                for (String wfId : newWorkflowIds) {
                    ProcessCreateDto dto = new ProcessCreateDto(
                            "Proceso " + wfId, "Timpo de generación: " + nowIso, wfId, List.of()
                    );
                    processService.create(dto);
                }
            }

            return new SyncSummary(entities.size(), toCreate, toUpdate);

        } catch (Exception e) {
            throw new RuntimeException("Error procesando workflows", e);
        }
    }

    private static OffsetDateTime parseDate(String s) {
        if (s == null) return null;
        try {
            return OffsetDateTime.parse(s);
        } catch (Exception ignored) {
            return null;
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