package com.fernandoschilder.ipaconsolebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "namespaces")
public class NamespaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "namespace_id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // Evita recursion infinita en JSON
    @JsonIgnore
    @ManyToMany(mappedBy = "permission_namespaces", fetch = FetchType.LAZY)
    private Set<PermissionEntity> permissions;

    // Si tus procesos tienen un campo "namespace", esto se mantiene
    @JsonIgnore
    @OneToMany(mappedBy = "namespace", fetch = FetchType.LAZY)
    private Set<ProcessEntity> processes;

    public NamespaceEntity() {
    }

    public NamespaceEntity(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionEntity> permissions) {
        this.permissions = permissions;
    }

    public Set<ProcessEntity> getProcesses() {
        return processes;
    }

    public void setProcesses(Set<ProcessEntity> processes) {
        this.processes = processes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamespaceEntity that = (NamespaceEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
