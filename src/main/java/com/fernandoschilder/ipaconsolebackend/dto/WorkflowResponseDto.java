package com.fernandoschilder.ipaconsolebackend.dto;

public record WorkflowResponseDto(
        String id,
        String name,
        boolean active,
        boolean archived,
        String rawJson
) {}