package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProcessUpdateDto(String name,   // optional edit
                               String description, // optional edit
                               @NotNull List<ParameterEditDto> parameters) {
}