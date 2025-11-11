package com.fernandoschilder.ipaconsolebackend.repository;

import java.time.Instant;

public interface ExecutionSummary {
    String getExecutionId();
    Instant getStartedAt();
    Instant getStoppedAt();
    String getWorkflowId();
    String getStatus();
    Boolean getFinished();
    Instant getCreatedAt();
}
