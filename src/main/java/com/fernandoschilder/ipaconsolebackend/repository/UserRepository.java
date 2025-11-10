package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fernandoschilder.ipaconsolebackend.model.PermissionAction;

public interface UserRepository
        extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);

    // Fetch namespace IDs that the given user has via roles -> permissions -> namespaces
    @Query("select distinct n.id from UserEntity u " +
            "join u.roles r join r.permissions p join p.namespace n " +
            "where u.username = :username and p.action = :action")
    List<Long> findNamespaceIdsByUsernameAndAction(@Param("username") String username,
                                                    @Param("action") PermissionAction action);

    @Query("select case when count(p) > 0 then true else false end from UserEntity u " +
            "join u.roles r join r.permissions p " +
            "where u.username = :username and p.namespace is null and p.action = :action")
    boolean userHasGlobalPermission(@Param("username") String username,
                                    @Param("action") PermissionAction action);
}
