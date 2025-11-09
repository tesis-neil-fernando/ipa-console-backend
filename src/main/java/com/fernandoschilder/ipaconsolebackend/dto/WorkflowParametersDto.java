package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowParametersDto {
    private final Map<String, String> values;

    public WorkflowParametersDto(Map<String, String> values) {
        this.values = values;
    }

    @JsonAnyGetter
    public Map<String, String> getValues() {
        return values;
    }
}
