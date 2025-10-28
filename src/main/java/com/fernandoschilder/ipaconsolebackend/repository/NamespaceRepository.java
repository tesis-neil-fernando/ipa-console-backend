package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NamespaceRepository extends JpaRepository<NamespaceEntity,Long> {
}
