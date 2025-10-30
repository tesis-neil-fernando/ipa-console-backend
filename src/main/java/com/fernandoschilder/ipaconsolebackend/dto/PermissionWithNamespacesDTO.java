package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.Set;

/** DTO representing a permission type and the namespaces assigned to it. */
public record PermissionWithNamespacesDTO(String type, Set<String> namespaces) {
}
