package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.Set;

/**
 * Role data transfer object as a Java record.
 * Immutable, concise representation used for API responses/requests.
 */
public record RoleDTO(Long id, String name, String description, Set<String> permissions) {
}
