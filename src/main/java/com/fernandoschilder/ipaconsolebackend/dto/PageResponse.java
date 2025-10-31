package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        List<T> items,
        Integer page,
        Integer size,
        Long totalItems,
        Integer totalPages,
        String nextCursor
) {
    public static <T> PageResponse<T> of(List<T> items, String nextCursor) {
        return new PageResponse<>(items, null, null, null, null, nextCursor);
    }
}
