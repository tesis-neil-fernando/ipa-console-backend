package com.fernandoschilder.ipaconsolebackend.utils;

import com.fernandoschilder.ipaconsolebackend.dto.WorkflowResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;

public final class WorkflowMapper {
    private WorkflowMapper() {}

    public static WorkflowResponseDto toResponseDto(WorkflowEntity e, boolean includeRaw) {
        return new WorkflowResponseDto(
                e.getId(),
                e.getName(),
                e.isActive(),
                e.isArchived(),
                includeRaw ? e.getRawJson() : null
        );
    }
}