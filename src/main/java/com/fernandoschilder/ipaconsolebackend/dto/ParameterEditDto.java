package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.constraints.NotNull;

public record ParameterEditDto(@NotNull Long id, String name, String value, String type) {
}