package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository
        extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);

    // Fetch namespace IDs that the given user has via roles -> permissions -> namespaces
    @Query("select distinct n.id from UserEntity u " +
            "join u.roles r join r.permissions p join p.namespaces n " +
            "where u.username = :username and p.type = :permType")
    List<Long> findNamespaceIdsByUsernameAndPermissionType(@Param("username") String username,
                                                            @Param("permType") String permType);
}
