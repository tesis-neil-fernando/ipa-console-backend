package com.fernandoschilder.ipaconsolebackend.repository;

import java.time.OffsetDateTime;

/**
 * Projection interface for execution listing. Excludes large LOB fields like rawJson.
 */
public interface ExecutionSummary {
    String getExecutionId();
    OffsetDateTime getStartedAt();
    OffsetDateTime getStoppedAt();
    String getProcessName();
    String getStatus();
    Boolean getFinished();
    OffsetDateTime getCreatedAt();
}
