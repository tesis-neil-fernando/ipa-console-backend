package com.fernandoschilder.ipaconsolebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "permissions", uniqueConstraints = {@UniqueConstraint(columnNames = {"namespace_id", "action"})})
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private PermissionAction action;

    @JsonIgnore
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<RoleEntity> roles = new HashSet<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_id")
    private NamespaceEntity namespace;

    public PermissionEntity() {
    }

    public PermissionEntity(PermissionAction action) {
        this.action = action;
    }

    public PermissionEntity(PermissionAction action, NamespaceEntity namespace) {
        this.action = action;
        this.namespace = namespace;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PermissionAction getAction() {
        return action;
    }

    public void setAction(PermissionAction action) {
        this.action = action;
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

    public NamespaceEntity getNamespace() {
        return namespace;
    }

    public void setNamespace(NamespaceEntity namespace) {
        this.namespace = namespace;
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
