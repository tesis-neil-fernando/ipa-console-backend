package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.Set;

/** DTO used to fully replace a user (username, password, roles) */
public record UserReplaceDto(String username, String password, Set<String> roles) {
}
