package com.fernandoschilder.ipaconsolebackend.dto;

public record ExecutionBriefDto(
        String id,
        String startedAt,
        boolean finished,
        String status
) {}