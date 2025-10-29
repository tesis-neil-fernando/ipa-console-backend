package com.fernandoschilder.ipaconsolebackend.service;

// Lombok removed
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class N8nWebhookService {

    private final WebClient n8nWebhookClient;

    public N8nWebhookService(@Qualifier("n8nWebhookClient") WebClient n8nWebhookClient) {
        this.n8nWebhookClient = n8nWebhookClient;
    }

    public ResponseEntity<N8nApiService.ApiResponse<String>> postWebhook(String path, Object body) {
        try {
            String clean = trimSlashes(path);
            var req = n8nWebhookClient.post().uri(uriBuilder -> uriBuilder.path("/webhook/").path(clean).build());

            String response = (body == null ? req : req.bodyValue(body)).retrieve().onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), cr -> cr.bodyToMono(String.class).map(b -> new RuntimeException("Error al llamar webhook (" + cr.statusCode() + "): " + b))).bodyToMono(String.class).block();

            return ResponseEntity.ok(new N8nApiService.ApiResponse<>(true, "Webhook ejecutado", response));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new N8nApiService.ApiResponse<>(false, "Error al ejecutar webhook: " + e.getMessage(), null));
        }
    }

    private String trimSlashes(String s) {
        if (s == null) return "";
        return s.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
