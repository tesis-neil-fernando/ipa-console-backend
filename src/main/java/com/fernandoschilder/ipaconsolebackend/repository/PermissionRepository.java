package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.PermissionAction;
import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByNamespaceAndAction(NamespaceEntity namespace, PermissionAction action);

    Optional<PermissionEntity> findByNamespaceIsNullAndAction(PermissionAction action);

    boolean existsByNamespaceAndAction(NamespaceEntity namespace, PermissionAction action);

    boolean existsByNamespaceIsNullAndAction(PermissionAction action);
}