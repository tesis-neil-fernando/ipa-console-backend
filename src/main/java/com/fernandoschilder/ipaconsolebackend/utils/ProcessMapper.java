package com.fernandoschilder.ipaconsolebackend.utils;

import com.fernandoschilder.ipaconsolebackend.dto.*;
import com.fernandoschilder.ipaconsolebackend.model.*;

public final class ProcessMapper {

    private ProcessMapper() {}

    // -- Workflow
    public static WorkflowResponseDto toWorkflowDto(WorkflowEntity w) {
        if (w == null) return null;

        return new WorkflowResponseDto(
                w.getId(),
                w.getName(),
                w.isActive(),
                w.isArchived(),
                null
        );
    }

    // -- Process
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