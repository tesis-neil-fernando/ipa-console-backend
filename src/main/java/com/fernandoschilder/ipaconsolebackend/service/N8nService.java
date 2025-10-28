package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.response.N8nWorkflowsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;


@Service
public class N8nService {
    private final WebClient n8nClient;

    public N8nService(@Qualifier("n8nClient") WebClient n8nClient) {
        this.n8nClient = n8nClient;
    }

    public Mono<String> getWorkflowsRaw() {
        return n8nClient.get()
                .uri("/api/v1/workflows")
                .retrieve()
                .bodyToMono(String.class);
    }

    public ResponseEntity<String> getExecutions(
            Boolean includeData, String status, String workflowId, String projectId,
            Integer limit, String cursor) {

        return n8nClient.get()
                .uri(b -> {
                    var u = b.path("/api/v1/executions");
                    if (includeData != null) u.queryParam("includeData", includeData);
                    if (status != null)      u.queryParam("status", status);
                    if (workflowId != null)  u.queryParam("workflowId", workflowId);
                    if (projectId != null)   u.queryParam("projectId", projectId);
                    if (limit != null)       u.queryParam("limit", limit);
                    if (cursor != null)      u.queryParam("cursor", cursor);
                    return u.build();
                })
                .exchangeToMono(cr -> cr.toEntity(String.class))
                .block(); // <- estamos en MVC, aquí sí bloqueamos
    }
}