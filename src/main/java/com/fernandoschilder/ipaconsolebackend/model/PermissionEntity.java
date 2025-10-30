package com.fernandoschilder.ipaconsolebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "permissions")
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @Column(name = "type", unique = true, nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String type;

    @JsonIgnore
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<RoleEntity> roles = new HashSet<>();
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "permission_namespaces", joinColumns = @JoinColumn(name = "permission_id"), inverseJoinColumns = @JoinColumn(name = "namespace_id"))
    private Set<NamespaceEntity> namespaces = new HashSet<>();

    public PermissionEntity() {
    }

    public PermissionEntity(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    public void addRole(RoleEntity role) {
        if (role == null) return;
        if (this.roles == null) this.roles = new HashSet<>();
        this.roles.add(role);
        if (role.getPermissions() == null) role.setPermissions(new HashSet<>());
        role.getPermissions().add(this);
    }

    public void removeRole(RoleEntity role) {
        if (role == null) return;
        if (this.roles != null) this.roles.remove(role);
        if (role.getPermissions() != null) role.getPermissions().remove(this);
    }

    public Set<NamespaceEntity> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Set<NamespaceEntity> namespaces) {
        this.namespaces = namespaces;
    }

    public void addNamespace(NamespaceEntity ns) {
        if (ns == null) return;
        if (this.namespaces == null) this.namespaces = new HashSet<>();
        this.namespaces.add(ns);
        if (ns.getPermissions() == null) ns.setPermissions(new HashSet<>());
        ns.getPermissions().add(this);
    }

    public void removeNamespace(NamespaceEntity ns) {
        if (ns == null) return;
        if (this.namespaces != null) this.namespaces.remove(ns);
        if (ns.getPermissions() != null) ns.getPermissions().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionEntity that = (PermissionEntity) o;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
