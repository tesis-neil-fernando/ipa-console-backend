package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExecutionBriefDto(
        String id,
        String startedAt,
        boolean finished,
        String status
) {}