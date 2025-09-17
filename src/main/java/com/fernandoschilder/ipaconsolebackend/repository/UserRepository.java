package com.fernandoschilder.ipaconsolebackend.repository;

import java.util.Optional;

import com.fernandoschilder.ipaconsolebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

}
