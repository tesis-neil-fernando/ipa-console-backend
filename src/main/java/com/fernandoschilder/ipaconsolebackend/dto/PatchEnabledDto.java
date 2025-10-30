package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.constraints.NotNull;

/** Small DTO to represent enabled/disabled patch requests for a user. */
public record PatchEnabledDto(@NotNull Boolean enabled) {
}
