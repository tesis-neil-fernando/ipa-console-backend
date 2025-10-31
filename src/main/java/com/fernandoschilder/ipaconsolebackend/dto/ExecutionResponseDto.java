package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExecutionResponseDto(
        String id,
        Instant startedAt,
        Instant finishedAt,
        String processName,
        String status,
        Boolean finished
) {}