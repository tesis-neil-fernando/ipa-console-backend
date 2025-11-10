package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sessions")
public class SessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @Column(name = "jti", unique = true, nullable = false, length = 128)
    private String jti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "ip_address", length = 128)
    private String ipAddress;

    @Column(name = "user_agent", length = 1024)
    private String userAgent;

    @Column(name = "os", length = 256)
    private String os;

    @Column(name = "issued_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date issuedAt;

    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Column(name = "revoked")
    private boolean revoked = false;

    @Column(name = "last_access_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAccessAt;

    public SessionEntity() {
    }

    public SessionEntity(String jti, UserEntity user, String ipAddress, String userAgent, String os, Date issuedAt, Date expiresAt) {
        this.jti = jti;
        this.user = user;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.os = os;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.lastAccessAt = issuedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Date getLastAccessAt() {
        return lastAccessAt;
    }

    public void setLastAccessAt(Date lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
    }
}
