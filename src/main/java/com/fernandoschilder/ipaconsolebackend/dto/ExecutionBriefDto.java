package com.fernandoschilder.ipaconsolebackend.dto;

import java.time.OffsetDateTime;

public record ExecutionBriefDto(
        Long id,
        String startedAt,
        boolean finished,
        String status
) {}