package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkflowDto (
        String id,
        String name,
        boolean active,
        boolean isArchived,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Map<String, Object> connections,
        Map<String, Object> settings,
        Map<String, Object> staticData,
        Map<String, Object> meta,
        Map<String, Object> pinData,
        String versionId,
        int triggerCount,
        List<Object> tags
) {}
