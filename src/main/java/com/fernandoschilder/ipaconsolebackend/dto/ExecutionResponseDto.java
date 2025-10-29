package com.fernandoschilder.ipaconsolebackend.dto;

import java.time.OffsetDateTime;

public record ExecutionResponseDto(
        String id,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        String processName,
        String status,
        Boolean finished
) {}