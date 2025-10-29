package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {
    @NotBlank(message = "username is required")
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 100)
    private String password;
}
