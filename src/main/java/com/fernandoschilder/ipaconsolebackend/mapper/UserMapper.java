package com.fernandoschilder.ipaconsolebackend.mapper;

import com.fernandoschilder.ipaconsolebackend.dto.UserViewDTO;
import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserViewDTO toViewDTO(UserEntity e) {
        if (e == null) return null;

        Set<String> roles = (e.getUser_roles() == null) ? Set.of()
                : e.getUser_roles().stream()
                .filter(Objects::nonNull)
                .map(RoleEntity::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> namespaces = Optional.ofNullable(e.getUser_roles()).orElse(Set.of()).stream()
                .filter(Objects::nonNull)
                .flatMap(r -> Optional.ofNullable(r.getPermissions()).orElse(Set.of()).stream())
                .filter(Objects::nonNull)
                .flatMap(p -> Optional.ofNullable(p.getPermission_namespaces()).orElse(Set.of()).stream())
                .filter(Objects::nonNull)
                .map(ns -> ns.getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new UserViewDTO(e.getId(), e.getUsername(), e.isEnabled(), roles, namespaces);
    }
}
