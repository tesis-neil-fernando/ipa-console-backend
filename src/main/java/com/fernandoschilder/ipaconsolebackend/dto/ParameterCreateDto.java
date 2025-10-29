package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.constraints.NotBlank;

public record ParameterCreateDto(
        @NotBlank String name,
        @NotBlank String value,
        @NotBlank String type
) {}