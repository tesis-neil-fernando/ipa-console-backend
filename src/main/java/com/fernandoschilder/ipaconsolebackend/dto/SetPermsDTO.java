package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.List;

/**
 * DTO used to set or add permissions on a role.
 */
public record SetPermsDTO(List<String> permissions) {
}
