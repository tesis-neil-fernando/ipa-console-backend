package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO used when creating a Role.
 */
public record CreateRoleDTO(@NotBlank String name) {
}
