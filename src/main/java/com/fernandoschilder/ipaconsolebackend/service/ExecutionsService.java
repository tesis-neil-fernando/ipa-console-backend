package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionsListResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
// Lombok removed - add explicit constructor and logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ExecutionsService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionsService.class);

    private final N8nApiService n8nApiService;
    private final ProcessRepository processRepository;
    private final ObjectMapper objectMapper;

    public ExecutionsService(N8nApiService n8nApiService, ProcessRepository processRepository, ObjectMapper objectMapper) {
        this.n8nApiService = n8nApiService;
        this.processRepository = processRepository;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<N8nApiService.ApiResponse<ExecutionsListResponseDto>> listExecutions(
            Boolean includeData, String status, String workflowId, String projectId,
            Integer limit, String cursor) {

    try {
        var envelope = n8nApiService.fetchExecutions(includeData, status, workflowId, projectId, limit, cursor);

        var mapped = envelope.data().stream()
            .map(e -> {
            String name = processRepository.findByWorkflow_Id(e.workflowId())
                .map(ProcessEntity::getName)
                .orElse(null); // one-to-one => unique or null if not registered yet
            return new ExecutionResponseDto(
                e.id(),
                parseTs(e.startedAt()),
                parseTs(e.stoppedAt()),
                name,
                e.status(),
                e.finished()
            );
            })
            .toList();

        var payload = new ExecutionsListResponseDto(mapped, envelope.nextCursor());
        return ResponseEntity.ok(new N8nApiService.ApiResponse<>(true, "Ejecuciones obtenidas correctamente", payload));

    } catch (N8nClientException ex) {
        log.error("N8n client error while listing executions", ex);
        var err = new N8nApiService.ApiResponse<ExecutionsListResponseDto>(false,
            "Error contacting n8n: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    } catch (Exception ex) {
        var err = new N8nApiService.ApiResponse<ExecutionsListResponseDto>(false,
            "Error al procesar ejecuciones: " + ex.getMessage(), null);
        return ResponseEntity.internalServerError().body(err);
    }
    }

    private static OffsetDateTime parseTs(String iso) {
        if (iso == null) return null;
        try {
            return OffsetDateTime.parse(iso);
        } catch (java.time.format.DateTimeParseException ex) {
            log.debug("Failed to parse timestamp '{}'", iso, ex);
            return null;
        }
    }

    // parsing is delegated to N8nApiService.getExecutionsParsed
}

