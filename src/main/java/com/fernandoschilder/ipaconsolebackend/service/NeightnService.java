package com.fernandoschilder.ipaconsolebackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class NeightnService {

    private WebClient webClient;

    public NeightnService(WebClient webClient) {
        this.webClient = webClient;
    }
    public Mono<String> getWorkflows() {
        return webClient.get().uri("/api/v1/workflows").retrieve().bodyToMono(String.class);
    }
}
