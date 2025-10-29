package com.fernandoschilder.ipaconsolebackend.response;

import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;

import java.util.List;

public class N8nWorkflowsResponse {
    private List<WorkflowEntity> data;
    private String nextCursor;

    public N8nWorkflowsResponse() {}

    public List<WorkflowEntity> getData() {
        return data;
    }

    public void setData(List<WorkflowEntity> data) {
        this.data = data;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }
}