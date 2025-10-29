package com.fernandoschilder.ipaconsolebackend.service;

public class N8nClientException extends RuntimeException {
    private final int statusCode;

    public N8nClientException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public N8nClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public N8nClientException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
