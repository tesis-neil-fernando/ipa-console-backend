package com.fernandoschilder.ipaconsolebackend.service;

public class N8nClientException extends RuntimeException {
    private final int statusCode;
    private final String upstreamBody;

    public N8nClientException(String message) {
        super(message);
        this.statusCode = -1;
        this.upstreamBody = null;
    }

    public N8nClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.upstreamBody = null;
    }

    public N8nClientException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.upstreamBody = null;
    }

    public N8nClientException(String message, int statusCode, String upstreamBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.upstreamBody = upstreamBody;
    }

    public String getUpstreamBody() {
        return upstreamBody;
    }

    public static N8nClientException from(org.springframework.web.reactive.function.client.WebClientResponseException wre) {
        String body = null;
        try {
            body = wre.getResponseBodyAsString();
        } catch (Exception ignored) {}
        String sanitized = sanitizeUpstreamBody(body);
        return new N8nClientException("Upstream error: " + wre.getMessage(), wre.getRawStatusCode(), sanitized, wre);
    }

    private static String sanitizeUpstreamBody(String body) {
        if (body == null) return null;
        // Truncate to 2048 chars to avoid flooding logs/responses
        int max = 2048;
        String trimmed = body.length() > max ? body.substring(0, max) + "...[truncated]" : body;
        return trimmed;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
