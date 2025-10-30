package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.List;

/** Wrapper DTO used to set permissions with their namespaces on a role. */
public record SetPermsWithNamespacesDTO(List<PermissionWithNamespacesDTO> permissions) {
}
