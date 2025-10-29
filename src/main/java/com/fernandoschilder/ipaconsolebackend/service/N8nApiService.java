package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.nio.charset.StandardCharsets;

@Service
public class N8nApiService {

    private static final Logger log = LoggerFactory.getLogger(N8nApiService.class);

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
                    .map(body -> WebClientResponseException.create(
                        cr.statusCode().value(), cr.statusCode().toString(), cr.headers().asHttpHeaders(),
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
            )
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(10))
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
                    .map(body -> WebClientResponseException.create(
                        cr.statusCode().value(), cr.statusCode().toString(), cr.headers().asHttpHeaders(),
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
            )
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(10))
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
                    .map(body -> WebClientResponseException.create(
                        cr.statusCode().value(), cr.statusCode().toString(), cr.headers().asHttpHeaders(),
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
            )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
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
                    .map(body -> WebClientResponseException.create(
                        cr.statusCode().value(), cr.statusCode().toString(), cr.headers().asHttpHeaders(),
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
            )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            var envelope = objectMapper.readValue(response, N8nExecutionsEnvelope.class);
            return envelope;

        } catch (Exception e) {
            // Log less verbosely for expected upstream client errors
            if (e instanceof WebClientResponseException wre) {
                if (wre.getRawStatusCode() >= 500) {
                    log.error("Error fetching executions from n8n", wre);
                } else {
                    log.warn("Error fetching executions from n8n: {}", wre.getMessage());
                    try { log.debug("Upstream body: {}", wre.getResponseBodyAsString()); } catch (Exception ignore) {}
                }
                throw N8nClientException.from(wre);
            }
            if (e instanceof RuntimeException && e.getMessage() != null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\((\\d{3})").matcher(e.getMessage());
                if (m.find()) {
                    try {
                        int code = Integer.parseInt(m.group(1));
                        throw new N8nClientException("Error al obtener ejecuciones: " + e.getMessage(), code, e);
                    } catch (NumberFormatException ignored) {
                        // fall through
                    }
                }
            }
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
                    .map(body -> WebClientResponseException.create(
                        cr.statusCode().value(), cr.statusCode().toString(), cr.headers().asHttpHeaders(),
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
            )
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(10))
            .block();

            var envelope = objectMapper.readValue(response, N8nWorkflowsEnvelope.class);
            return envelope;

        } catch (Exception e) {
            if (e instanceof WebClientResponseException wre) {
                if (wre.getRawStatusCode() >= 500) {
                    log.error("Error fetching workflows from n8n", wre);
                } else {
                    log.warn("Error fetching workflows from n8n: {}", wre.getMessage());
                    try { log.debug("Upstream body: {}", wre.getResponseBodyAsString()); } catch (Exception ignore) {}
                }
                throw N8nClientException.from(wre);
            }
            if (e instanceof RuntimeException && e.getMessage() != null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\((\\d{3})").matcher(e.getMessage());
                if (m.find()) {
                    try {
                        int code = Integer.parseInt(m.group(1));
                        throw new N8nClientException("Error al obtener workflows: " + e.getMessage(), code, e);
                    } catch (NumberFormatException ignored) {
                        // fall through
                    }
                }
            }
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
                    .map(body -> WebClientResponseException.create(
                        cr.statusCode().value(), cr.statusCode().toString(), cr.headers().asHttpHeaders(),
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
            )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
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
                    .map(body -> WebClientResponseException.create(
                        clientResponse.statusCode().value(), clientResponse.statusCode().toString(), clientResponse.headers().asHttpHeaders(),
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
            )
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(10))
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
                    .map(body -> WebClientResponseException.create(
                        clientResponse.statusCode().value(), clientResponse.statusCode().toString(), clientResponse.headers().asHttpHeaders(),
                        body == null ? null : body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
            )
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(10))
            .block();

            return ResponseEntity.ok(new ApiResponse<>(true, "Workflow desactivado correctamente", response));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al desactivar el workflow: " + e.getMessage(), null));
        }
    }

    public static record ApiResponse<T>(boolean success, String message, T data) {}

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
