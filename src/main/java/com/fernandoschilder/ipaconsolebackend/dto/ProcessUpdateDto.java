package com.fernandoschilder.ipaconsolebackend.dto;

import jakarta.validation.Valid;
import java.util.List;

public record ProcessUpdateDto(String name,   // optional edit
                               String description, // optional edit
                               @Valid List<ParameterUpdateDto> parameters) {
}