package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface WorkflowRepository extends JpaRepository<WorkflowEntity, String> {

	/**
	 * Try to insert a minimal workflow row. Uses native SQL ON CONFLICT DO NOTHING so this
	 * is safe to call concurrently from multiple threads without causing unique constraint
	 * violations. Runs in a new transaction to avoid holding locks in the caller transaction.
	 */
	@Modifying
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Query(value = "INSERT INTO workflows (workflow_id, name, active, is_archived, raw_json) VALUES (:id, :name, :active, :archived, :rawJson) ON CONFLICT (workflow_id) DO NOTHING", nativeQuery = true)
	void insertIfNotExists(@Param("id") String id, @Param("name") String name, @Param("active") boolean active, @Param("archived") boolean archived, @Param("rawJson") String rawJson);

}

