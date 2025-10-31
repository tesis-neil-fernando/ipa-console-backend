package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProcessResponseDto(
        Long id,
        String name,
        String description,
        WorkflowResponseDto workflow,
        List<ParameterResponseDto> parameters,
        ExecutionBriefDto executionBrief
) {}