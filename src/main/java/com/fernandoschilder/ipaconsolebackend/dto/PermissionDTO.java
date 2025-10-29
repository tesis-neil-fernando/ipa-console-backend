package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.Set;

/** Permission data transfer object implemented as a record. */
public record PermissionDTO(Long id, String type, Set<String> namespaces) {
}
