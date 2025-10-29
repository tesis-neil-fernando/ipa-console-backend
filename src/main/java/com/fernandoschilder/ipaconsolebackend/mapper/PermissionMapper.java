package com.fernandoschilder.ipaconsolebackend.mapper;

import com.fernandoschilder.ipaconsolebackend.dto.PermissionDTO;
import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    @Mapping(target = "namespaces", source = "permission_namespaces")
    PermissionDTO toPermissionDto(PermissionEntity p);

    default String namespaceToString(NamespaceEntity n) {
        return n == null ? null : n.getName();
    }
}
