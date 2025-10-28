package com.fernandoschilder.ipaconsolebackend.response;

import com.fernandoschilder.ipaconsolebackend.model.N8nWorkflowEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class N8nWorkflowsResponse {
    private List<N8nWorkflowEntity> data;
    private String nextCursor;
}