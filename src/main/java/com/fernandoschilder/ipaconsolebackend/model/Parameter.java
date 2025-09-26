package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="parameters")
public class Parameter {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name="parameter_id")
    private Long id;
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id")
    private Process process;

    @NonNull
    @Column(name = "name")
    private String name;
    @NonNull
    @Column(name = "value")
    private String value;
}
