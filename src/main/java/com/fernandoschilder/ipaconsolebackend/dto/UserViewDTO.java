package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.Set;

/** DTO de lectura para usuarios (no expone password). Implemented as a record. */
public record UserViewDTO(Long id, String username, boolean enabled, Set<String> roles, Set<String> namespaces) {
}
