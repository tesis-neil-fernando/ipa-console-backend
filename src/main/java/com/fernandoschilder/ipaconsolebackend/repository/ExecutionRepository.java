package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, Long> {
    Optional<ExecutionEntity> findByExecutionId(String executionId);
    boolean existsByExecutionId(String executionId);

    // Use projections (JPQL selects) to avoid loading the LOB column when listing executions.
    // Use startedAt for listing and cursor pagination so ordering reflects the execution start time
    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.workflowId as workflowId, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e order by e.startedAt desc")
    List<ExecutionSummary> findAllSummaries(Pageable pageable);

    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.workflowId as workflowId, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e where e.startedAt < :startedAt order by e.startedAt desc")
    List<ExecutionSummary> findSummariesByStartedAtBefore(@Param("startedAt") Instant startedAt, Pageable pageable);

    // Variants filtered by workflowId
    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.workflowId as workflowId, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e where e.workflowId = :workflowId order by e.startedAt desc")
    List<ExecutionSummary> findAllSummariesByWorkflowId(@Param("workflowId") String workflowId, Pageable pageable);

    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.workflowId as workflowId, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e where e.workflowId = :workflowId and e.startedAt < :startedAt order by e.startedAt desc")
    List<ExecutionSummary> findSummariesByWorkflowIdAndStartedAtBefore(@Param("workflowId") String workflowId, @Param("startedAt") Instant startedAt, Pageable pageable);

    // WorkflowIds-based variants for namespace-scoped filtering (service will build workflowId lists from processes)
    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.workflowId as workflowId, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e where e.workflowId in :workflowIds order by e.startedAt desc")
    List<ExecutionSummary> findAllSummariesByWorkflowIds(@Param("workflowIds") java.util.Collection<String> workflowIds, Pageable pageable);

    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.workflowId as workflowId, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e where e.workflowId in :workflowIds and e.startedAt < :startedAt order by e.startedAt desc")
    List<ExecutionSummary> findSummariesByStartedAtBeforeAndWorkflowIds(@Param("startedAt") Instant startedAt, @Param("workflowIds") java.util.Collection<String> workflowIds, Pageable pageable);

    // Resolve only the startedAt timestamp for a given execution id — avoids loading the full entity (and its LOB)
    @Query("select e.startedAt from ExecutionEntity e where e.executionId = :executionId")
    Optional<Instant> findStartedAtByExecutionId(@Param("executionId") String executionId);

    // Projection-based helper: load execution summaries in a time range (avoids loading LOB/raw_json)
    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.workflowId as workflowId, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e where e.startedAt >= :start and e.startedAt < :end order by e.startedAt desc")
    java.util.List<ExecutionSummary> findSummariesByStartedAtBetween(@Param("start") Instant start, @Param("end") Instant end);
}
