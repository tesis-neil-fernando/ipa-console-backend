package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.List;

/** DTO to create role with permissions in one request */
public record CreateRoleWithPermsDTO(String name, List<String> permissions) {
}
