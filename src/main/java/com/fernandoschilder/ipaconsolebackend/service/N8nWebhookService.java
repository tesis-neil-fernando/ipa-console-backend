package com.fernandoschilder.ipaconsolebackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
            // For client exceptions originating from n8n, avoid printing full stacktrace for 4xx statuses
            if (e.getStatusCode() >= 500) {
                log.error("Error executing webhook {}: {}", path, e.getMessage(), e);
            } else {
                log.warn("Error executing webhook {}: {}", path, e.getMessage());
            }
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
                                    .map(b -> WebClientResponseException.create(cr.statusCode().value(), cr.statusCode().toString(), cr.headers().asHttpHeaders(), b == null ? null : b.getBytes(java.nio.charset.StandardCharsets.UTF_8), java.nio.charset.StandardCharsets.UTF_8)))
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return response;
        } catch (Exception e) {
            // If the error is an upstream HTTP error, avoid full stack for client errors
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException wre) {
                if (wre.getRawStatusCode() >= 500) {
                    log.error("Failed to call webhook {}", path, wre);
                } else {
                    log.warn("Failed to call webhook {}: {}", path, wre.getMessage());
                    try {
                        String ub = wre.getResponseBodyAsString();
                        log.debug("Upstream body: {}", ub);
                    } catch (Exception ignored) {
                    }
                }
            } else {
                log.error("Failed to call webhook {}", path, e);
            }
            // If WebClient produced a WebClientResponseException, preserve the upstream status
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException wre) {
                throw N8nClientException.from(wre);
            }
            // Some mappings use RuntimeException with a message containing the status, parse it
            if (e instanceof RuntimeException && e.getMessage() != null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\((\\d{3})").matcher(e.getMessage());
                if (m.find()) {
                    try {
                        int code = Integer.parseInt(m.group(1));
                        throw new N8nClientException("Error al ejecutar webhook: " + e.getMessage(), code, e);
                    } catch (NumberFormatException ignored) {
                        // fall through to generic wrapper
                    }
                }
            }
            throw new N8nClientException("Error al ejecutar webhook: " + e.getMessage(), e);
        }
    }

    private String trimSlashes(String s) {
        if (s == null) return "";
        return s.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
