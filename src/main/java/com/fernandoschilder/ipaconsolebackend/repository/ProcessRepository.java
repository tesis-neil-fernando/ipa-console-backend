package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository extends JpaRepository<ProcessEntity, Long> {
    Optional<ProcessEntity> findByWorkflow_Id(String workflowId);

    boolean existsByWorkflow_Id(String workflowId);

    @EntityGraph(attributePaths = {"workflow", "parameters"})
    Optional<ProcessEntity> findById(Long id);

    @EntityGraph(attributePaths = {"workflow", "parameters"})
    List<ProcessEntity> findAll();

    @EntityGraph(attributePaths = {"workflow", "parameters"})
    java.util.List<ProcessEntity> findDistinctByWorkflow_Tags_NameIn(java.util.Collection<String> names);

    @EntityGraph(attributePaths = {"workflow", "parameters"})
    java.util.List<ProcessEntity> findAllByNamespace_IdIn(java.util.Collection<Long> namespaceIds);

    @EntityGraph(attributePaths = {"workflow", "parameters"})
    java.util.List<ProcessEntity> findAllByWorkflow_IdIn(java.util.Collection<String> workflowIds);
}
