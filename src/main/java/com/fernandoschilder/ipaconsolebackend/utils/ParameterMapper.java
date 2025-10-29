package com.fernandoschilder.ipaconsolebackend.utils;

import com.fernandoschilder.ipaconsolebackend.dto.ParameterResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.ParameterEntity;

public final class ParameterMapper {

    private ParameterMapper() {}

    public static ParameterResponseDto toResponseDto(ParameterEntity e) {
        if (e == null) return null;

        return new ParameterResponseDto(
                e.getId(),
                e.getName(),
                e.getValue(),
                e.getType()
        );
    }
}