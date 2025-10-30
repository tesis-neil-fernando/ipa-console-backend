package com.fernandoschilder.ipaconsolebackend.dto;

/**
 * Simple generic wrapper for RBAC responses.
 */
public record RbacResponse<T>(T data, String message) {
}
