package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface N8nWorkflowRepository extends JpaRepository<WorkflowEntity, String> {}

