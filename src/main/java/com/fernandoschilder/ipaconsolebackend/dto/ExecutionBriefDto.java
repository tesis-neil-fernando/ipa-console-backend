package com.fernandoschilder.ipaconsolebackend.dto;

public record ExecutionBriefDto(
        Long id,
        String startedAt,
        boolean finished,
        String status
) {}