package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;
    @NonNull
    @Column(name = "time")
    private Instant time;
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;
    @NonNull
    @Column(name = "initiator")
    private String initiator;
    @NonNull
    @Column(name = "state")
    private String state;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
