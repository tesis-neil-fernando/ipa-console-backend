package com.fernandoschilder.ipaconsolebackend.dto;

import java.time.Instant;

public record ExecutionResponseDto(
        String id,
        Instant startedAt,
        Instant finishedAt,
        String processName,
        String status,
        Boolean finished
) {}