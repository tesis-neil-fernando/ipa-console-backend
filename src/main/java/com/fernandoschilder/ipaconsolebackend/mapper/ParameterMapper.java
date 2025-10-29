package com.fernandoschilder.ipaconsolebackend.mapper;

import com.fernandoschilder.ipaconsolebackend.dto.ParameterResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.ParameterEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParameterMapper {
    ParameterResponseDto toResponseDto(ParameterEntity e);
}
