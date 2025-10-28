package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.response.N8nWorkflowsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class N8nService {
    private final WebClient n8nClient;

    public N8nService(@Qualifier("n8nClient") WebClient n8nClient) {
        this.n8nClient = n8nClient;
    }

    // public Mono<N8nWorkflowsResponse> getWorkflows() {
    //     return n8nClient.get()
    //             .uri("/api/v1/workflows")
    //             .retrieve()
    //             .bodyToMono(N8nWorkflowsResponse.class);
    // }

    public Mono<String> getWorkflowsRaw() {
        return n8nClient.get()
                .uri("/api/v1/workflows")
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> startProcess(String id) {
        return n8nClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/executions").queryParam("workflowId", id).build())
                .retrieve()
                .bodyToMono(String.class);
    }
}