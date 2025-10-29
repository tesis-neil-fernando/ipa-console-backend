package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProcessCreateDto(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String workflowId,
        @Valid List<ParameterCreateDto> parameters
) {}