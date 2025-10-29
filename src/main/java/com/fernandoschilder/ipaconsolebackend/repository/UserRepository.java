package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);
}
