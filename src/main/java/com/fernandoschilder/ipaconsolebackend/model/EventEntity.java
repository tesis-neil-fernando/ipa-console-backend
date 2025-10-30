package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "events", indexes = {@Index(name = "idx_events_process", columnList = "process_id")})
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "time")
    private Instant time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessEntity process;

    @Column(name = "type")
    private String type;

    @Column(name = "message")
    private String message;

    public EventEntity() {
    }

    public EventEntity(Instant time, ProcessEntity process, String type, String message) {
        this.time = time;
        this.process = process;
        this.type = type;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventEntity that = (EventEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
