package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(name = "parameters", indexes = {@Index(name = "idx_parameters_process", columnList = "process_id")})
public class ParameterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parameter_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessEntity process;

    @Column(name = "name", nullable = false)
    @NotNull
    @Size(max = 255)
    private String name;

    @Column(name = "value", nullable = false)
    @NotNull
    @Size(max = 2000)
    private String value;

    @Column(name = "type", nullable = false)
    @NotNull
    @Size(max = 100)
    private String type;

    public ParameterEntity() {
    }

    public ParameterEntity(ProcessEntity process, String name, String value, String type) {
        this.process = process;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterEntity that = (ParameterEntity) o;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}