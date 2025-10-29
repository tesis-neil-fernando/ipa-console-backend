package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO used when creating a user. Implemented as a record with validation annotations on components.
 */
public record UserCreateDto(
        @NotBlank(message = "username is required") @Size(min = 3, max = 100) String username,
        @NotBlank(message = "password is required") @Size(min = 6, max = 100) String password
) {
}
