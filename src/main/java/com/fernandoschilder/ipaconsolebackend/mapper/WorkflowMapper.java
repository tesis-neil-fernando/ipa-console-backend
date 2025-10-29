package com.fernandoschilder.ipaconsolebackend.mapper;

import com.fernandoschilder.ipaconsolebackend.dto.WorkflowResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {TagMapper.class})
public interface WorkflowMapper {

    @Named("toDto")
    @Mapping(target = "rawJson", expression = "java(null)")
    WorkflowResponseDto toDto(WorkflowEntity e);

    @Named("toDtoWithRaw")
    @Mapping(target = "rawJson", source = "rawJson")
    WorkflowResponseDto toDtoWithRaw(WorkflowEntity e);
}
