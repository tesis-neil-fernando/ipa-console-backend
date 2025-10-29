package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NamespaceRepository extends JpaRepository<NamespaceEntity, Long> {
    Optional<NamespaceEntity> findByName(String name);
    boolean existsByName(String name);
}
