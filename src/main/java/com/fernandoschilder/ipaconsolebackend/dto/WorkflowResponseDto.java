package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkflowResponseDto(
        String id,
        String name,
        boolean active,
        boolean archived,
        String rawJson,
        Set<TagResponseDto> tags
) {}