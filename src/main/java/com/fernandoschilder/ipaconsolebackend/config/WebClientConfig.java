package com.fernandoschilder.ipaconsolebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Value("${fernandoschilder.app.n8n-apikey}")
    private String n8nApiKey;
    @Value("${fernandoschilder.app.n8n-webhookkey}")
    private String n8nWebhookKey;

    @Value("${fernandoschilder.app.n8n-url}")
    private String n8nUrl;

    @Bean("n8nApiClient")
    public WebClient n8nClient(WebClient.Builder builder) {
        return builder
                .baseUrl(n8nUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-N8N-API-KEY",  n8nApiKey)
                .build();
    }

    @Bean("n8nWebhookClient")
    public WebClient webhookClient(WebClient.Builder builder) {
        return builder
                .baseUrl(n8nUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-N8N-WEBHOOK-KEY",n8nWebhookKey)
                .build();
        }
}