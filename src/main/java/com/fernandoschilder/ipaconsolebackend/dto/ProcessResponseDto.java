package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.List;

public record ProcessResponseDto(
        Long id,
        String name,
        String description,
        WorkflowDto workflow,
        List<ParameterResponseDto> parameters
) {}
