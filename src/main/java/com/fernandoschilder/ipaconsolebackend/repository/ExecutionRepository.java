package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, Long> {
    Optional<ExecutionEntity> findByExecutionId(String executionId);
    boolean existsByExecutionId(String executionId);

    // Use projections (JPQL selects) to avoid loading the LOB column when listing executions.
    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.processName as processName, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e order by e.createdAt desc")
    List<ExecutionSummary> findAllSummaries(Pageable pageable);

    @Query("select e.executionId as executionId, e.startedAt as startedAt, e.stoppedAt as stoppedAt, e.processName as processName, e.status as status, e.finished as finished, e.createdAt as createdAt from ExecutionEntity e where e.createdAt < :createdAt order by e.createdAt desc")
    List<ExecutionSummary> findSummariesByCreatedAtBefore(@Param("createdAt") OffsetDateTime createdAt, Pageable pageable);

    // Resolve only the createdAt timestamp for a given execution id — avoids loading the full entity (and its LOB)
    @Query("select e.createdAt from ExecutionEntity e where e.executionId = :executionId")
    Optional<OffsetDateTime> findCreatedAtByExecutionId(@Param("executionId") String executionId);
}
