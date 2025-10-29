package com.fernandoschilder.ipaconsolebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/** DTO de lectura para usuarios (no expone password). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserViewDTO {
    private Long id;
    private String username;
    private boolean enabled;
    private Set<String> roles; // nombres de rol
}
