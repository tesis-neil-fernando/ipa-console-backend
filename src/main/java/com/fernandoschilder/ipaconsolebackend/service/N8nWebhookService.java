package com.fernandoschilder.ipaconsolebackend.service;

// Lombok removed
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;


@Service
public class N8nWebhookService {

    private static final Logger log = LoggerFactory.getLogger(N8nWebhookService.class);

    private final WebClient n8nWebhookClient;

    public N8nWebhookService(@Qualifier("n8nWebhookClient") WebClient n8nWebhookClient) {
        this.n8nWebhookClient = n8nWebhookClient;
    }

    public ResponseEntity<N8nApiService.ApiResponse<String>> postWebhook(String path, Object body) {
        try {
            String response = postWebhookRaw(path, body);
            return ResponseEntity.ok(new N8nApiService.ApiResponse<>(true, "Webhook ejecutado", response));
        } catch (N8nClientException e) {
            log.error("Error executing webhook {}: {}", path, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new N8nApiService.ApiResponse<>(false, "Error al ejecutar webhook: " + e.getMessage(), null));
        }
    }

    /**
     * Low-level helper: call the webhook and return raw response or throw N8nClientException.
     * This is useful for other services that prefer exceptions over ResponseEntity.
     */
    public String postWebhookRaw(String path, Object body) {
        try {
            String clean = trimSlashes(path);
            var req = n8nWebhookClient.post().uri(uriBuilder -> uriBuilder.path("/webhook/").path(clean).build());

            String response = (body == null ? req : req.bodyValue(body))
                    .retrieve()
                    .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), cr -> cr.bodyToMono(String.class)
                            .map(b -> new RuntimeException("Error al llamar webhook (" + cr.statusCode() + "): " + b)))
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return response;
        } catch (Exception e) {
            log.error("Failed to call webhook {}", path, e);
            throw new N8nClientException("Error al ejecutar webhook: " + e.getMessage(), e);
        }
    }

    private String trimSlashes(String s) {
        if (s == null) return "";
        return s.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
