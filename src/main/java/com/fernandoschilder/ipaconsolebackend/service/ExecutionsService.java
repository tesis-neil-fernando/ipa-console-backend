package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionsListResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExecutionsService {

    private final N8nApiService n8nApiService;
    private final ProcessRepository processRepository;
    private final ObjectMapper objectMapper;

    public ResponseEntity<N8nApiService.ApiResponse<ExecutionsListResponseDto>> listExecutions(
            Boolean includeData, String status, String workflowId, String projectId,
            Integer limit, String cursor) {

    var n8nResp = n8nApiService.getExecutionsParsed(includeData, status, workflowId, projectId, limit, cursor);
    if (n8nResp.getBody() == null || !n8nResp.getBody().isSuccess() || n8nResp.getBody().getData() == null) {
        var fail = new N8nApiService.ApiResponse<ExecutionsListResponseDto>(false,
            n8nResp.getBody() != null ? n8nResp.getBody().getMessage() : "Empty response from n8n", null);
        return ResponseEntity.status(n8nResp.getStatusCode()).body(fail);
    }

    try {
        var envelope = n8nResp.getBody().getData();

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

    } catch (Exception ex) {
        var err = new N8nApiService.ApiResponse<ExecutionsListResponseDto>(false,
            "Error al procesar ejecuciones: " + ex.getMessage(), null);
        return ResponseEntity.internalServerError().body(err);
    }
    }

    private static OffsetDateTime parseTs(String iso) {
        return iso == null ? null : OffsetDateTime.parse(iso);
    }

    // parsing is delegated to N8nApiService.getExecutionsParsed
}

