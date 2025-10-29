package com.fernandoschilder.ipaconsolebackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NamespaceDTO {
    private Long id;
    private String name;
    private String description;
}
