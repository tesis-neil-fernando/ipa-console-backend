package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.List;

public record ExecutionsListResponseDto(
        List<ExecutionResponseDto> data,
        String nextCursor
) {}