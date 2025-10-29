package com.fernandoschilder.ipaconsolebackend.utils;

import com.fernandoschilder.ipaconsolebackend.dto.*;
import com.fernandoschilder.ipaconsolebackend.model.*;

import java.util.Set;
import java.util.stream.Collectors;

public final class ProcessMapper {

    private ProcessMapper() {}

    public static WorkflowResponseDto toWorkflowDto(WorkflowEntity w) {
        if (w == null) return null;
        Set<TagResponseDto> tagDtos = w.getTags().stream()
                .map(t -> new TagResponseDto(t.getId(), t.getName()))
                .collect(Collectors.toSet());

        return new WorkflowResponseDto(
                w.getId(),
                w.getName(),
                w.isActive(),
                w.isArchived(),
                null,
                tagDtos
        );
    }

    public static ProcessResponseDto toResponseDto(ProcessEntity p) {
        return new ProcessResponseDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                toWorkflowDto(p.getWorkflow()),
                p.getParameters() == null ? java.util.List.of()
                        : p.getParameters().stream()
                        .map(ParameterMapper::toResponseDto)
                        .toList(),
                null
        );
    }

}