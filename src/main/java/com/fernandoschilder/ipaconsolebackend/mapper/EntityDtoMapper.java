package com.fernandoschilder.ipaconsolebackend.mapper;

import com.fernandoschilder.ipaconsolebackend.dto.PermissionDTO;
import com.fernandoschilder.ipaconsolebackend.dto.RoleDTO;
import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {

    public RoleDTO toRoleDto(RoleEntity r) {
        Set<String> perms = (r.getPermissions() == null) ? Set.of()
                : r.getPermissions().stream()
                .filter(p -> p != null && p.getType() != null)
                .map(PermissionEntity::getType)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

    return new RoleDTO(r.getId(), r.getName(), perms);
    }

    public PermissionDTO toPermissionDto(PermissionEntity p) {
    Set<String> namespaces = (p.getNamespaces() == null) ? Set.of()
        : p.getNamespaces().stream()
                .filter(ns -> ns != null && ns.getName() != null)
                .map(ns -> ns.getName())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        return new PermissionDTO(p.getId(), p.getType(), namespaces);
    }
}
