package com.fernandoschilder.ipaconsolebackend.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AssignNamespacesDTO {
    private Set<String> namespaces;
}
