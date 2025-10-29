package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class N8nApiService {

    private final WebClient n8nClient;
    private final ObjectMapper objectMapper;

    public N8nApiService(@Qualifier("n8nApiClient") WebClient n8nClient, ObjectMapper objectMapper) {
        this.n8nClient = n8nClient;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<ApiResponse<String>> getWorkflowsRaw() {
        try {
            String response = n8nClient.get()
                    .uri("/api/v1/workflows")
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            cr -> cr.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Error al obtener workflows (" + cr.statusCode() + "): " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok(new ApiResponse<>(true, "Workflows obtenidos correctamente", response));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener workflows: " + e.getMessage(), null));
        }
    }

    /**
     * Typed helper: fetch workflows and parse into an envelope containing the data list.
     */
    public ResponseEntity<ApiResponse<N8nWorkflowsEnvelope>> getWorkflowsParsed() {
        try {
            String response = n8nClient.get()
                    .uri("/api/v1/workflows")
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            cr -> cr.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Error al obtener workflows (" + cr.statusCode() + "): " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            var envelope = objectMapper.readValue(response, N8nWorkflowsEnvelope.class);
            return ResponseEntity.ok(new ApiResponse<>(true, "Workflows obtenidos correctamente", envelope));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener workflows: " + e.getMessage(), null));
        }
    }

    public ResponseEntity<ApiResponse<String>> getExecutions(
            Boolean includeData, String status, String workflowId, String projectId,
            Integer limit, String cursor) {

        try {
            String response = n8nClient.get()
                    .uri(b -> {
                        var u = b.path("/api/v1/executions");
                        if (includeData != null) u.queryParam("includeData", includeData);
                        if (status != null) u.queryParam("status", status);
                        if (workflowId != null) u.queryParam("workflowId", workflowId);
                        if (projectId != null) u.queryParam("projectId", projectId);
                        if (limit != null) u.queryParam("limit", limit);
                        if (cursor != null) u.queryParam("cursor", cursor);
                        return u.build();
                    })
                    .retrieve()
                    .onStatus(
                            statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                            cr -> cr.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Error al obtener ejecuciones (" + cr.statusCode() + "): " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok(new ApiResponse<>(true, "Ejecuciones obtenidas correctamente", response));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener ejecuciones: " + e.getMessage(), null));
        }
    }

    /**
     * Throwing method: fetch executions and return parsed envelope or throw N8nClientException.
     */
    public N8nExecutionsEnvelope fetchExecutions(Boolean includeData, String status, String workflowId, String projectId,
                                                 Integer limit, String cursor) {
        try {
            String response = n8nClient.get()
                    .uri(b -> {
                        var u = b.path("/api/v1/executions");
                        if (includeData != null) u.queryParam("includeData", includeData);
                        if (status != null) u.queryParam("status", status);
                        if (workflowId != null) u.queryParam("workflowId", workflowId);
                        if (projectId != null) u.queryParam("projectId", projectId);
                        if (limit != null) u.queryParam("limit", limit);
                        if (cursor != null) u.queryParam("cursor", cursor);
                        return u.build();
                    })
                    .retrieve()
                    .onStatus(
                            statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                            cr -> cr.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Error al obtener ejecuciones (" + cr.statusCode() + "): " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            var envelope = objectMapper.readValue(response, N8nExecutionsEnvelope.class);
            return envelope;

        } catch (Exception e) {
            log.error("Error fetching executions from n8n", e);
            throw new N8nClientException("Error al obtener ejecuciones: " + e.getMessage(), e);
        }
    }

    /**
     * Throwing method: fetch workflows and return parsed envelope or throw N8nClientException.
     */
    public N8nWorkflowsEnvelope fetchWorkflows() {
        try {
            String response = n8nClient.get()
                    .uri("/api/v1/workflows")
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            cr -> cr.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Error al obtener workflows (" + cr.statusCode() + "): " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            var envelope = objectMapper.readValue(response, N8nWorkflowsEnvelope.class);
            return envelope;

        } catch (Exception e) {
            log.error("Error fetching workflows from n8n", e);
            throw new N8nClientException("Error al obtener workflows: " + e.getMessage(), e);
        }
    }

    /**
     * Typed helper: fetch executions and parse into a POJO envelope.
     * Returns ResponseEntity with ApiResponse.data as {@link N8nExecutionsEnvelope} on success.
     */
    public ResponseEntity<ApiResponse<N8nExecutionsEnvelope>> getExecutionsParsed(
            Boolean includeData, String status, String workflowId, String projectId,
            Integer limit, String cursor) {

        try {
            String response = n8nClient.get()
                    .uri(b -> {
                        var u = b.path("/api/v1/executions");
                        if (includeData != null) u.queryParam("includeData", includeData);
                        if (status != null) u.queryParam("status", status);
                        if (workflowId != null) u.queryParam("workflowId", workflowId);
                        if (projectId != null) u.queryParam("projectId", projectId);
                        if (limit != null) u.queryParam("limit", limit);
                        if (cursor != null) u.queryParam("cursor", cursor);
                        return u.build();
                    })
                    .retrieve()
                    .onStatus(
                            statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                            cr -> cr.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Error al obtener ejecuciones (" + cr.statusCode() + "): " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            var envelope = objectMapper.readValue(response, N8nExecutionsEnvelope.class);

            return ResponseEntity.ok(new ApiResponse<>(true, "Ejecuciones obtenidas correctamente", envelope));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener ejecuciones: " + e.getMessage(), null));
        }
    }

    public ResponseEntity<ApiResponse<String>> activateWorkflow(String workflowId) {
        try {
            String response = n8nClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/workflows/{id}/activate")
                            .build(workflowId))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new RuntimeException(
                                            "Error al activar workflow (" + clientResponse.statusCode() + "): " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok(new ApiResponse<>(true, "Workflow activado correctamente", response));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al activar el workflow: " + e.getMessage(), null));
        }
    }

    public ResponseEntity<ApiResponse<String>> deactivateWorkflow(String workflowId) {
        try {
            String response = n8nClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/workflows/{id}/deactivate")
                            .build(workflowId))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new RuntimeException(
                                            "Error al desactivar workflow (" + clientResponse.statusCode() + "): " + body))
                    )
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok(new ApiResponse<>(true, "Workflow desactivado correctamente", response));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al desactivar el workflow: " + e.getMessage(), null));
        }
    }

    @Data
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
    }

    /* --- typed n8n shapes used by the client --- */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record N8nExecution(String id, String status, Boolean finished,
                                      String startedAt, String stoppedAt, String workflowId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record N8nExecutionsEnvelope(java.util.List<N8nExecution> data, String nextCursor) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record N8nTag(String id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record N8nWorkflow(String id, String name, Boolean active, Boolean isArchived, java.util.List<N8nTag> tags) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record N8nWorkflowsEnvelope(java.util.List<N8nWorkflow> data) {}
}
