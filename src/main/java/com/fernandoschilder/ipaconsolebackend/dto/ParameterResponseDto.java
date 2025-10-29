package com.fernandoschilder.ipaconsolebackend.dto;

public record ParameterResponseDto(
   Long id,
   String name,
   String value,
   String type
) {}

