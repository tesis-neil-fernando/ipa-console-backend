package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="parameters")
public class ParameterEntity {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name="parameter_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessEntity process;

    @NonNull
    @Column(name = "name", nullable = false)
    private String name;

    @NonNull
    @Column(name = "value", nullable = false)
    private String value;

    @NonNull
    @Column(name = "type", nullable = false)
    private String type;
}