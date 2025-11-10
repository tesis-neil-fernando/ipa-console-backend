package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    Optional<SessionEntity> findByJti(String jti);
    void deleteByJti(String jti);
    java.util.List<com.fernandoschilder.ipaconsolebackend.model.SessionEntity> findByUser_Id(Long userId);
}
