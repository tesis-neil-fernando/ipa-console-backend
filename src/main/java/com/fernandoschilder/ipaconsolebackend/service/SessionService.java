package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.model.SessionEntity;
import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import com.fernandoschilder.ipaconsolebackend.repository.SessionRepository;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "sessionActive", key = "#jti")
    public SessionEntity createSession(Long userId, String jti, Date issuedAt, Date expiresAt, String ipAddress, String userAgent, String os) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        SessionEntity s = new SessionEntity(jti, user, ipAddress, userAgent, os, issuedAt, expiresAt);
        return sessionRepository.save(s);
    }

    @Cacheable(value = "sessionActive", key = "#jti")
    public boolean isActive(String jti) {
        return sessionRepository.findByJti(jti)
                .map(s -> !s.isRevoked() && (s.getExpiresAt() == null || s.getExpiresAt().after(new Date())))
                .orElse(false);
    }

    @CacheEvict(value = "sessionActive", key = "#jti")
    @Transactional
    public void revokeSession(String jti) {
        sessionRepository.findByJti(jti).ifPresent(s -> {
            s.setRevoked(true);
            sessionRepository.save(s);
        });
    }

    @CacheEvict(value = "sessionActive", key = "#jti")
    @Transactional
    public void hardDeleteSession(String jti) {
        sessionRepository.deleteByJti(jti);
    }

    public java.util.List<SessionEntity> listSessionsForUser(Long userId) {
        return sessionRepository.findByUser_Id(userId);
    }

    /**
     * Return only active sessions for a user (not revoked and not expired).
     */
    public java.util.List<SessionEntity> listActiveSessionsForUser(Long userId) {
        java.util.Date now = new java.util.Date();
        return sessionRepository.findByUser_Id(userId).stream()
                .filter(s -> !s.isRevoked())
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().after(now))
                .collect(java.util.stream.Collectors.toList());
    }

    @CacheEvict(value = "sessionActive", allEntries = true)
    @Transactional
    public void revokeOtherSessions(Long userId, String keepJti) {
        java.util.List<SessionEntity> list = sessionRepository.findByUser_Id(userId);
        for (SessionEntity s : list) {
            if (keepJti != null && keepJti.equals(s.getJti())) continue;
            if (!s.isRevoked()) {
                s.setRevoked(true);
                sessionRepository.save(s);
            }
        }
    }

    @CacheEvict(value = "sessionActive", allEntries = true)
    @Transactional
    public void hardDeleteOtherSessions(Long userId, String keepJti) {
        java.util.List<SessionEntity> list = sessionRepository.findByUser_Id(userId);
        for (SessionEntity s : list) {
            if (keepJti != null && keepJti.equals(s.getJti())) continue;
            sessionRepository.deleteByJti(s.getJti());
        }
    }

}
