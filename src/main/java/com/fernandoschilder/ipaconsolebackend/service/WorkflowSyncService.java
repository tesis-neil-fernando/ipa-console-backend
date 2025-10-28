package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.model.N8nWorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.repository.N8nWorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.response.N8nWorkflowsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowSyncService {

    private final N8nService n8nService;
    private final N8nWorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // para serializar a String

    @Transactional
    public int pullAndSave() {
        String rawResponse = n8nService.getWorkflowsRaw().block();
        if (rawResponse == null) return 0;

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode data = root.path("data");
            if (!data.isArray()) return 0;

            List<N8nWorkflowEntity> entities = new ArrayList<>();
            for (JsonNode wfNode : data) {
                N8nWorkflowEntity e = new N8nWorkflowEntity();
                e.setId(wfNode.path("id").asText());
                e.setName(wfNode.path("name").asText());
                e.setActive(wfNode.path("active").asBoolean());
                e.setArchived(wfNode.path("isArchived").asBoolean());
                e.setRawJson(wfNode.toString());
                entities.add(e);
            }

            workflowRepository.saveAll(entities);
            return entities.size();

        } catch (Exception e) {
            throw new RuntimeException("Error procesando workflows", e);
        }
    }
}
