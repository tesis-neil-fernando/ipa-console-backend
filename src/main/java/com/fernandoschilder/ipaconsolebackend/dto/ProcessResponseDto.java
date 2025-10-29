package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.List;

public record ProcessResponseDto(
        Long id,
        String name,
        String description,
        WorkflowResponseDto workflow,
        List<ParameterResponseDto> parameters,
        ExecutionBriefDto executionBrief
) {}