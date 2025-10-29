package com.fernandoschilder.ipaconsolebackend.utils;

import com.fernandoschilder.ipaconsolebackend.dto.TagResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.WorkflowResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;

import java.util.Set;
import java.util.stream.Collectors;

public final class WorkflowMapper {
    private WorkflowMapper() {}

    public static WorkflowResponseDto toResponseDto(WorkflowEntity e, boolean includeRaw) {
        Set<TagResponseDto> tagDtos = e.getTags().stream()
                .map(t -> new TagResponseDto(t.getId(), t.getName()))
                .collect(Collectors.toSet());

        return new WorkflowResponseDto(
                e.getId(),
                e.getName(),
                e.isActive(),
                e.isArchived(),
                includeRaw ? e.getRawJson() : null,
                tagDtos
        );
    }
}