package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "executions", indexes = {@Index(name = "idx_execution_id", columnList = "execution_id", unique = true), @Index(name = "idx_executions_workflow", columnList = "workflow_id"), @Index(name = "idx_executions_status", columnList = "status"), @Index(name = "idx_executions_created_at", columnList = "created_at")})
public class ExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_pk")
    private Long id;

    @Column(name = "execution_id", nullable = false, unique = true)
    private String executionId;

    @Column(name = "workflow_id")
    private String workflowId;

    @Column(name = "status")
    private String status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "stopped_at")
    private Instant stoppedAt;

    @Column(name = "finished")
    private Boolean finished;

    @Column(name = "process_name")
    private String processName;

    @Column(name = "mode")
    private String mode;

    @Column(name = "retry_of")
    private String retryOf;

    @Column(name = "retry_success_id")
    private String retrySuccessId;

    @Column(name = "wait_till")
    private Instant waitTill;

    @Lob
    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

    @Column(name = "created_at")
    private Instant createdAt;

    public ExecutionEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(Instant stoppedAt) {
        this.stoppedAt = stoppedAt;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRetryOf() {
        return retryOf;
    }

    public void setRetryOf(String retryOf) {
        this.retryOf = retryOf;
    }

    public String getRetrySuccessId() {
        return retrySuccessId;
    }

    public void setRetrySuccessId(String retrySuccessId) {
        this.retrySuccessId = retrySuccessId;
    }

    public Instant getWaitTill() {
        return waitTill;
    }

    public void setWaitTill(Instant waitTill) {
        this.waitTill = waitTill;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionEntity that = (ExecutionEntity) o;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
