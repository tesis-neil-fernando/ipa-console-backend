package com.fernandoschilder.ipaconsolebackend.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityExistsException;
import com.fernandoschilder.ipaconsolebackend.service.N8nClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // Add correlationId to ErrorResponse for easier tracing. Also keep details map for optional fields.
        public static record ErrorResponse(String timestamp, int status, String error, String message, String path, String correlationId, Map<String, String> details) {}

        private ErrorResponse build(HttpStatus status, String message, String path) {
            return new ErrorResponse(Instant.now().toString(), status.value(), status.getReasonPhrase(), message, path, extractCorrelationId(), null);
        }

        private ErrorResponse build(HttpStatus status, String message, String path, Map<String, String> details) {
            return new ErrorResponse(Instant.now().toString(), status.value(), status.getReasonPhrase(), message, path, extractCorrelationId(), details);
        }

        private String extractCorrelationId() {
            // Prefer MDC value set by CorrelationIdFilter; fall back to empty string
            try {
                String cid = MDC.get("correlationId");
                return cid == null ? "" : cid;
            } catch (Exception ex) {
                return "";
            }
        }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        var resp = build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponse> handleExists(EntityExistsException ex, HttpServletRequest request) {
        var resp = build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        var resp = build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler(N8nClientException.class)
    public ResponseEntity<ErrorResponse> handleN8nFailure(N8nClientException ex, HttpServletRequest request) {
        int sc = ex.getStatusCode();
        HttpStatus status = null;
        Map<String, String> details = null;

        // If the exception already carries a valid HTTP status, use it.
        if (sc >= 400 && sc < 600) {
            status = HttpStatus.resolve(sc);
        }

        // If the exception exposes an upstream body directly, use it (factory sets it).
        if (ex instanceof com.fernandoschilder.ipaconsolebackend.service.N8nClientException && ((com.fernandoschilder.ipaconsolebackend.service.N8nClientException) ex).getUpstreamBody() != null) {
            String ub = ((com.fernandoschilder.ipaconsolebackend.service.N8nClientException) ex).getUpstreamBody();
            details = new HashMap<>();
            // Sanitize / summarize upstream body to avoid leaking sensitive internals
            details.put("upstreamSummary", summarize(ub));
        }

        // If no status was set, inspect the cause for WebClientResponseException to extract upstream status/body
        if (status == null) {
            Throwable cause = ex.getCause();
            if (cause instanceof WebClientResponseException wre) {
                int upstream = wre.getRawStatusCode();
                status = HttpStatus.resolve(upstream);
                if (status == null) status = HttpStatus.BAD_GATEWAY;
                try {
                    String body = wre.getResponseBodyAsString();
                    if (body != null && !body.isBlank()) {
                        if (details == null) details = new HashMap<>();
                        details.put("upstreamSummary", summarize(body));
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }

        if (status == null) status = HttpStatus.BAD_GATEWAY;

        // Log according to severity: don't print full stacktrace for client (4xx) upstream errors
        if (status.is5xxServerError()) {
            log.error("n8n client error: {}", ex.getMessage(), ex);
        } else {
            // expected client errors (4xx) - warn without stacktrace to avoid noisy console traces
            log.warn("n8n client error (status={}): {}", status.value(), ex.getMessage());
            if (details != null && details.containsKey("upstreamSummary")) {
                // still log upstream summary at debug level for diagnostics
                log.debug("n8n upstream summary: {}", details.get("upstreamSummary"));
            }
        }

        String msg = "Error contacting n8n: " + ex.getMessage();
        var resp = (details == null) ? build(status, msg, request.getRequestURI()) : build(status, msg, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(resp);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        var resp = build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), errors);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        var resp = build(HttpStatus.BAD_REQUEST, "Malformed JSON request: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        var resp = build(HttpStatus.BAD_REQUEST, "Invalid parameter: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        var resp = build(HttpStatus.BAD_REQUEST, "Missing parameter: " + ex.getParameterName(), request.getRequestURI());
        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> errors.put(cv.getPropertyPath().toString(), cv.getMessage()));
        var resp = build(HttpStatus.BAD_REQUEST, "Validation error", request.getRequestURI(), errors);
        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        var resp = build(HttpStatus.CONFLICT, "Data integrity violation: " + ex.getMostSpecificCause(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        var resp = build(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailure(AuthenticationException ex, HttpServletRequest request) {
        // Authentication failures (bad credentials, disabled account, etc.) -> 401
        log.info("Authentication failure for request {}: {}", request.getRequestURI(), ex.getMessage());
        var resp = build(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponse(WebClientResponseException ex, HttpServletRequest request) {
        // Upstream HTTP error from WebClient. Map the upstream status and include body if available.
        HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
        if (status == null) status = HttpStatus.BAD_GATEWAY;
        log.error("Upstream HTTP error calling external service: {} - {}", ex.getRawStatusCode(), ex.getMessage());
        Map<String, String> details = new HashMap<>();
        try {
            String body = ex.getResponseBodyAsString();
            if (body != null && !body.isBlank()) details.put("upstreamSummary", summarize(body));
        } catch (Exception ignore) {
            // ignore reading body errors
        }
        var resp = build(status, "Upstream service error: " + ex.getMessage(), request.getRequestURI(), details.isEmpty() ? null : details);
        return ResponseEntity.status(status).body(resp);
    }

    @ExceptionHandler({WebClientRequestException.class, WebClientException.class})
    public ResponseEntity<ErrorResponse> handleWebClientClientError(Exception ex, HttpServletRequest request) {
        // Network/I/O errors when calling an upstream service: map to 502 Bad Gateway
        log.error("WebClient communication error for request {}: {}", request.getRequestURI(), ex.getMessage());
        var resp = build(HttpStatus.BAD_GATEWAY, "Upstream communication error: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        // Generic fallback — log the exception for diagnostics
        log.error("Unhandled exception processing request {}", request.getRequestURI(), ex);
        var resp = build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }

    private String summarize(String body) {
        if (body == null) return "";
        String trimmed = body.trim();
        int max = 256;
        if (trimmed.length() <= max) return trimmed;
        return trimmed.substring(0, max) + "...";
    }
}
