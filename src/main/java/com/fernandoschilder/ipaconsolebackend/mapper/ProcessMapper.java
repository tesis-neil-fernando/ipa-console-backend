package com.fernandoschilder.ipaconsolebackend.mapper;

import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Qualifier;

@Mapper(componentModel = "spring", uses = {WorkflowMapper.class, ParameterMapper.class})
public interface ProcessMapper {
    @Mapping(target = "workflow", qualifiedByName = "toDto")
    @Mapping(target = "executionBrief", ignore = true)
    ProcessResponseDto toResponseDto(ProcessEntity p);
}
