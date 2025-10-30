package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.List;

/** Namespace DTO that includes processes list. */
public record NamespaceWithProcessesDTO(Long id, String name, List<ProcessResponseDto> processes) {
}
