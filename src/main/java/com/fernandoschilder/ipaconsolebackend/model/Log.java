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
    @Column(name = "process_id")
    private Long process_id;
    @NonNull
    @Column(name = "initiator")
    private String initiator;
    @NonNull
    @Column(name = "state")
    private String state;
    @Column(name = "user_id")
    private Long user_id;
}
