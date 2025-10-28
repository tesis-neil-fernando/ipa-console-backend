package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.WorkflowDto;
import com.fernandoschilder.ipaconsolebackend.dto.WorkflowsResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Service
public class NeightnService {
    private final WebClient webClient;

    public NeightnService(WebClient webClient) {
        this.webClient = webClient;
    }

    public WorkflowsResponse getWorkflowsPage(String cursor) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/workflows")
                        .queryParamIfPresent("cursor", cursor == null ? java.util.Optional.empty() : java.util.Optional.of(cursor))
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<WorkflowsResponse>() {})
                .block();
    }

    public List<WorkflowDto> getAllWorkflows() {
        List<WorkflowDto> out = new java.util.ArrayList<>();
        String cursor = null;
        do {
            WorkflowsResponse page = getWorkflowsPage(cursor);
            if (page == null) break;
            if (page.data() != null) out.addAll(page.data());
            cursor = page.nextCursor();
        } while (cursor != null);
        return out;
    }
}