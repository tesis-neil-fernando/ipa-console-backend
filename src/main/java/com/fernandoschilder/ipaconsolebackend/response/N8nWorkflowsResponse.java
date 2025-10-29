package com.fernandoschilder.ipaconsolebackend.response;

import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class N8nWorkflowsResponse {
    private List<WorkflowEntity> data;
    private String nextCursor;
}