package com.fernandoschilder.ipaconsolebackend.dto;

import com.fernandoschilder.ipaconsolebackend.model.TagEntity;

import java.util.Set;

public record WorkflowResponseDto(
        String id,
        String name,
        boolean active,
        boolean archived,
        String rawJson,
        Set<TagResponseDto> tags
) {}