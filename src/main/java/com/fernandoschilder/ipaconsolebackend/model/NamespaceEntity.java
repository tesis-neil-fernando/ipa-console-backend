package com.fernandoschilder.ipaconsolebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
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
    @NotNull
    @Size(max = 120)
    private String name;


    @JsonIgnore
    @ManyToMany(mappedBy = "namespaces", fetch = FetchType.LAZY)
    private Set<PermissionEntity> permissions = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "namespace", fetch = FetchType.LAZY)
    private Set<ProcessEntity> processes = new HashSet<>();

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

    // description removed per request

    public Set<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionEntity> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(PermissionEntity permission) {
        if (permission == null) return;
        if (this.permissions == null) this.permissions = new HashSet<>();
        this.permissions.add(permission);
        if (permission.getNamespaces() == null) permission.setNamespaces(new HashSet<>());
        permission.getNamespaces().add(this);
    }

    public void removePermission(PermissionEntity permission) {
        if (permission == null) return;
        if (this.permissions != null) this.permissions.remove(permission);
        if (permission.getNamespaces() != null) permission.getNamespaces().remove(this);
    }

    public Set<ProcessEntity> getProcesses() {
        return processes;
    }

    public void setProcesses(Set<ProcessEntity> processes) {
        this.processes = processes;
    }

    public void addProcess(ProcessEntity process) {
        if (process == null) return;
        if (this.processes == null) this.processes = new HashSet<>();
        this.processes.add(process);
        process.setNamespace(this);
    }

    public void removeProcess(ProcessEntity process) {
        if (process == null) return;
        if (this.processes != null) this.processes.remove(process);
        if (process != null) process.setNamespace(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamespaceEntity that = (NamespaceEntity) o;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
