package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository extends JpaRepository<ProcessEntity, Long> {
    @EntityGraph(attributePaths = {"workflow", "parameters"})
    Optional<ProcessEntity> findById(Long id);

    @EntityGraph(attributePaths = {"workflow", "parameters"})
    List<ProcessEntity> findAll();
}
