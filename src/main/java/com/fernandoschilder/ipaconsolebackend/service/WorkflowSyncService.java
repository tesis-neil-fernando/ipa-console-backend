package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.model.TagEntity;
import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.repository.WorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.repository.TagRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WorkflowSyncService {

    private final N8nApiService n8nService;
    private final WorkflowRepository workflowRepository;
    private final TagRepository tagRepository;
    private final ProcessService processService; // <-- NUEVO
    private final WorkflowSchedulerService workflowSchedulerService;
    private final ObjectMapper objectMapper;

    public WorkflowSyncService(N8nApiService n8nService, WorkflowRepository workflowRepository, TagRepository tagRepository, ProcessService processService, WorkflowSchedulerService workflowSchedulerService, ObjectMapper objectMapper) {
        this.n8nService = n8nService;
        this.workflowRepository = workflowRepository;
        this.tagRepository = tagRepository;
        this.processService = processService;
        this.workflowSchedulerService = workflowSchedulerService;
        this.objectMapper = objectMapper;
    }

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
                // Skip archived workflows: they should not be persisted locally.
                // Use Boolean check to avoid NPE when isArchived() is null.
                if (Boolean.TRUE.equals(wf.isArchived())) {
                    continue;
                }

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
                        String tagName = t == null ? null : t.name();
                        if (tagId == null) continue;
                        TagEntity managed = tagRepository.findById(tagId).orElse(null);
                        if (managed == null) {
                            // Create a minimal TagEntity if it doesn't exist yet. Use the
                            // tag name from n8n when available, otherwise fall back to id.
                            managed = new TagEntity();
                            managed.setId(tagId);
                            managed.setName(tagName != null ? tagName : tagId);
                            managed.setCreatedAt(Instant.now());
                            managed = tagRepository.save(managed);
                        }
                        tagSet.add(managed);
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
                String nowIso = Instant.now().toString();
                // re-load managed instances by their IDs
                List<WorkflowEntity> newWorkflows = new ArrayList<>();
                workflowRepository.findAllById(newWorkflowIds).forEach(newWorkflows::add);

                for (WorkflowEntity wf : newWorkflows) {
                    processService.ensureProcessForWorkflow(wf, "Proceso " + wf.getName(), "Tiempo de generación: " + nowIso);
                }
            }

                // Refresh in-memory schedules after sync so newly-created or updated processes
                // with Programación are picked up immediately.
                try {
                    workflowSchedulerService.refreshSchedules();
                } catch (Exception ignored) {
                    // non-fatal, scheduling refresh should not break sync
                }

            return new SyncSummary(entities.size(), toCreate, toUpdate);

        } catch (N8nClientException e) {
            // preserve client exception so GlobalExceptionHandler can translate it correctly
            throw e;
        } catch (Exception e) {
            // If the root cause is an upstream HTTP error, preserve its status in the N8nClientException
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException wre) {
                throw N8nClientException.from(wre);
            }
            if (e.getCause() instanceof org.springframework.web.reactive.function.client.WebClientResponseException wreCause) {
                throw N8nClientException.from(wreCause);
            }
            // Also parse RuntimeException messages produced by WebClient onStatus mapping
            if (e instanceof RuntimeException && e.getMessage() != null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\((\\d{3})").matcher(e.getMessage());
                if (m.find()) {
                    try {
                        int code = Integer.parseInt(m.group(1));
                        throw new N8nClientException("Error procesando workflows: " + e.getMessage(), code, e);
                    } catch (NumberFormatException ignored) {
                        // fall through
                    }
                }
            }
            // wrap unexpected errors into N8nClientException so the global handler returns a 502/Bad Gateway
            throw new N8nClientException("Error procesando workflows: " + e.getMessage(), e);
        }
    }

    public static record SyncSummary(int total, int created, int updated) {}

    /**
     * Try to extract a cron expression for a workflow.
     * Strategy:
     *  - look for tags whose name contains a cron like "cron=..." or "cron:..."
     *  - look into workflow name for "cron:..."
     *  - fallback: search rawJson for a cron-like pattern (5 or 6 space-separated fields)
     */
    private String extractCronForWorkflow(WorkflowEntity wf) {
        if (wf == null) return null;

        if (wf.getTags() != null) {
            for (var t : wf.getTags()) {
                if (t == null || t.getName() == null) continue;
                String tn = t.getName().trim();
                if (tn.toLowerCase().startsWith("cron=")) return tn.substring(5).trim();
                if (tn.toLowerCase().startsWith("cron:")) return tn.substring(5).trim();
            }
        }

        if (wf.getName() != null) {
            String n = wf.getName();
            int idx = n.toLowerCase().indexOf("cron:");
            if (idx >= 0) {
                return n.substring(idx + 5).trim();
            }
        }

        String raw = wf.getRawJson();
        if (raw != null) {
            // crude cron-like regex: five or six space-separated fields containing digits, *, /, -, , or ?
            Pattern p = Pattern.compile("([\\d\\*\\/,\\-?]+\\s+){4,5}[\\d\\*\\/,\\-?]+");
            Matcher m = p.matcher(raw);
            if (m.find()) {
                return m.group().trim();
            }
        }

        return null;
    }
}