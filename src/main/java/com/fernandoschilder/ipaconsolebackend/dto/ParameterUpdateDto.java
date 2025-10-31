package com.fernandoschilder.ipaconsolebackend.dto;

/**
 * Update DTO for parameters used in partial updates (PATCH). Fields are nullable
 * to indicate optional updates.
 */
public record ParameterUpdateDto(Long id, String name, String value, String type) {
}
