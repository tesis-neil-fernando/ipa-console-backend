package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.model.ExecutionEntity;
import com.fernandoschilder.ipaconsolebackend.repository.ExecutionRepository;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;

@Service
public class ExecutionSyncService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionSyncService.class);

    private final N8nApiService n8nApiService;
    private final ExecutionRepository executionRepository;
    private final ProcessRepository processRepository;
    private final ObjectMapper objectMapper;

    public ExecutionSyncService(N8nApiService n8nApiService, ExecutionRepository executionRepository,
                                ProcessRepository processRepository, ObjectMapper objectMapper) {
        this.n8nApiService = n8nApiService;
        this.executionRepository = executionRepository;
        this.processRepository = processRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SyncSummary pullAndSave() {
        int total = 0;
        int created = 0;

        try {
            String cursor = null;
            boolean stopBecauseExisting = false;

            do {
                var resp = n8nApiService.getExecutions(false, null, null, null, 10, cursor);
                if (resp == null || resp.getBody() == null || !resp.getStatusCode().is2xxSuccessful()) {
                    // include status and body when available to aid debugging
                    Object respBody = resp == null ? null : resp.getBody();
                    Integer status = resp == null ? null : resp.getStatusCodeValue();
                    log.warn("No response or non-2xx from n8n executions endpoint, status={}, body={}", status, respBody);
                    break;
                }
                var api = resp.getBody();
                if (api == null || !api.success()) {
                    log.warn("n8n returned unsuccessful payload when fetching executions: {}", api == null ? "null" : api.message());
                    break;
                }

                String raw = api.data();
                if (raw == null || raw.isBlank()) break;

                JsonNode env = objectMapper.readTree(raw);
                JsonNode data = env.get("data");
                String nextCursor = env.has("nextCursor") && !env.get("nextCursor").isNull() ? env.get("nextCursor").asText(null) : null;

                if (data == null || !data.isArray() || data.size() == 0) {
                    break;
                }

                for (JsonNode item : data) {
                    total++;
                    String execId = item.hasNonNull("id") ? item.get("id").asText() : null;
                    if (execId == null) continue;

                    if (executionRepository.existsByExecutionId(execId)) {
                        // Since n8n returns executions ordered by startedAt desc, once we hit an existing
                        // execution we assume older ones are already stored -> stop ingestion.
                        log.debug("Execution {} already exists locally. Stopping incremental ingestion.", execId);
                        stopBecauseExisting = true;
                        break;
                    }

                    ExecutionEntity e = new ExecutionEntity();
                    e.setExecutionId(execId);
                    e.setWorkflowId(item.hasNonNull("workflowId") ? item.get("workflowId").asText() : null);
                    e.setStatus(item.hasNonNull("status") ? item.get("status").asText() : null);
                    e.setFinished(item.hasNonNull("finished") ? item.get("finished").asBoolean() : null);
                    e.setRawJson(objectMapper.writeValueAsString(item));

                    // parse timestamps defensively
                    e.setStartedAt(parseTs(item.hasNonNull("startedAt") ? item.get("startedAt").asText() : null));
                    e.setStoppedAt(parseTs(item.hasNonNull("stoppedAt") ? item.get("stoppedAt").asText() : null));
                    // additional n8n fields
                    e.setMode(item.hasNonNull("mode") ? item.get("mode").asText() : null);
                    e.setRetryOf(item.hasNonNull("retryOf") ? item.get("retryOf").asText() : null);
                    e.setRetrySuccessId(item.hasNonNull("retrySuccessId") ? item.get("retrySuccessId").asText() : null);
                    e.setWaitTill(parseTs(item.hasNonNull("waitTill") ? item.get("waitTill").asText() : null));

                    // We no longer persist processName on executions; the process name is resolved dynamically
                    // from the Process -> Workflow relationship when listing executions.

                    e.setCreatedAt(Instant.now());
                    executionRepository.save(e);
                    created++;
                }

                if (stopBecauseExisting) break;
                cursor = nextCursor;
            } while (cursor != null && !cursor.isBlank());

            return new SyncSummary(total, created, stopBecauseExisting);

        } catch (Exception ex) {
            // translate nested WebClient errors into N8nClientException so caller/global handler can manage them
            if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException wre) {
                throw N8nClientException.from(wre);
            }
            throw new RuntimeException("Error ingesting executions: " + ex.getMessage(), ex);
        }
    }

    private static Instant parseTs(String iso) {
        if (iso == null) return null;
        try {
            return java.time.OffsetDateTime.parse(iso).toInstant();
        } catch (java.time.format.DateTimeParseException ex) {
            log.debug("Failed to parse timestamp '{}'", iso, ex);
            return null;
        }
    }

    public static record SyncSummary(int total, int created, boolean stoppedOnExisting) {}
}
