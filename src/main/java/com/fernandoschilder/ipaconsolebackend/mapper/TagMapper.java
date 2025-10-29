package com.fernandoschilder.ipaconsolebackend.mapper;

import com.fernandoschilder.ipaconsolebackend.dto.TagResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.TagEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponseDto toDto(TagEntity e);
}
