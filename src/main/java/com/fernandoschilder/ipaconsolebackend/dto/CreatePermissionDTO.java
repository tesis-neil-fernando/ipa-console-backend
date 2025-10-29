package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO used when creating a Permission.
 */
public record CreatePermissionDTO(@NotBlank String type) {
}
