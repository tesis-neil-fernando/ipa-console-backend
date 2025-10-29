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

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static record ErrorResponse(String timestamp, int status, String error, String message, String path, Map<String, String> details) {}

    private ErrorResponse build(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now().toString(), status.value(), status.getReasonPhrase(), message, path, null);
    }

    private ErrorResponse build(HttpStatus status, String message, String path, Map<String, String> details) {
        return new ErrorResponse(Instant.now().toString(), status.value(), status.getReasonPhrase(), message, path, details);
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
        // Log the root cause for diagnostics
        log.error("n8n client error: {}", ex.getMessage(), ex);
        int sc = ex.getStatusCode();
        HttpStatus status = null;
        if (sc >= 400 && sc < 600) status = HttpStatus.resolve(sc);
        if (status == null) status = HttpStatus.BAD_GATEWAY;
        String msg = "Error contacting n8n: " + ex.getMessage();
        var resp = build(status, msg, request.getRequestURI());
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        // Generic fallback — log the exception for diagnostics
        log.error("Unhandled exception processing request {}", request.getRequestURI(), ex);
        var resp = build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}
