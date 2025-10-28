package com.fernandoschilder.ipaconsolebackend.repository;

import java.util.Optional;

import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Boolean existsByUsername(String username);

}
