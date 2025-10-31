package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParameterResponseDto(
   Long id,
   String name,
   String value,
   String type
) {}

