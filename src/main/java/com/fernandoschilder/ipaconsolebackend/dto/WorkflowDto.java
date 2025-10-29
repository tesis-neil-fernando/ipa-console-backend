package com.fernandoschilder.ipaconsolebackend.dto;

public record WorkflowDto(
        String id,
        String name,
        boolean active,
        boolean archived
) {}
