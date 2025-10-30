package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.Set;

/**
 * Generic RBAC entity DTO used for simple responses when needed.
 */
public record EntityRbacDto(Long id, String name, String description, Set<String> permissions, Set<String> roles, Set<String> namespaces) {
}
