package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Transactional
    public SyncSummary pullAndSave() {
        try {
            var envelope = n8nService.fetchWorkflows();
            if (envelope == null || envelope.data() == null) {
                return new SyncSummary(0, 0, 0);
            }
            if (envelope == null || envelope.data() == null) {
                return new SyncSummary(0, 0, 0);
            }

            // A) Build all WorkflowEntity instances
            List<WorkflowEntity> entities = new ArrayList<>();
            for (var wf : envelope.data()) {
                WorkflowEntity e = new WorkflowEntity();
                e.setId(wf.id());
                e.setName(wf.name());
                e.setActive(wf.active() != null ? wf.active() : false);
                e.setArchived(wf.isArchived() != null ? wf.isArchived() : false);
                // store the raw JSON of the workflow as returned by n8n
                e.setRawJson(objectMapper.writeValueAsString(wf));

                // attach tags (as you already do)
                Set<TagEntity> tagSet = new HashSet<>();
                if (wf.tags() != null) {
                    for (var t : wf.tags()) {
                        String tagId = t == null ? null : t.id();
                        if (tagId == null) continue;
                        TagEntity managed = tagRepository.findById(tagId).orElse(null);
                        if (managed != null) tagSet.add(managed);
                    }
                }
                e.setTags(tagSet);

                entities.add(e);
            }

            // B) Work out which IDs are incoming vs already existing
            Set<String> incomingIds = entities.stream().map(WorkflowEntity::getId).collect(Collectors.toSet());

            Set<String> existingIds = new HashSet<>();
            workflowRepository.findAllById(incomingIds).forEach(w -> existingIds.add(w.getId()));

            int toUpdate = (int) entities.stream().filter(w -> existingIds.contains(w.getId())).count();
            int toCreate = entities.size() - toUpdate;

            // C) Upsert all workflows (save or update)
            workflowRepository.saveAll(entities);
            workflowRepository.flush();

            // D) Compute the newWorkflowIds right here (same scope)
            Set<String> newWorkflowIds = new HashSet<>(incomingIds);
            newWorkflowIds.removeAll(existingIds); // now only the newly created IDs remain

            // E) Create a Process for each NEW workflow (idempotent)
            if (!newWorkflowIds.isEmpty()) {
                String nowIso = OffsetDateTime.now().toString();
                // re-load managed instances by their IDs
                List<WorkflowEntity> newWorkflows = new ArrayList<>();
                workflowRepository.findAllById(newWorkflowIds).forEach(newWorkflows::add);

                for (WorkflowEntity wf : newWorkflows) {
                    processService.ensureProcessForWorkflow(wf, "Proceso " + wf.getId(), "Tiempo de generación: " + nowIso);
                }
            }

            return new SyncSummary(entities.size(), toCreate, toUpdate);

        } catch (N8nClientException e) {
            throw new RuntimeException("No se pudo obtener workflows desde n8n: " + e.getMessage(), e);
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