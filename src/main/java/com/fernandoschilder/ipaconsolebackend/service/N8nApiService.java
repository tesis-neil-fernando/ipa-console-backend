package com.fernandoschilder.ipaconsolebackend.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class N8nApiService {

    private final WebClient n8nClient;

    public N8nApiService(@Qualifier("n8nApiClient") WebClient n8nClient) {
        this.n8nClient = n8nClient;
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
}
