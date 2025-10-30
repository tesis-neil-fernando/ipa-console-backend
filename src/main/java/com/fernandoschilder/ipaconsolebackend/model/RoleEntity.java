package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String name;

    // description removed per request

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<UserEntity> users = new HashSet<>();
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<PermissionEntity> permissions = new HashSet<>();

    public RoleEntity() {
    }

    public RoleEntity(String name) {
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

    // description getter/setter removed

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

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
        if (permission.getRoles() == null) permission.setRoles(new HashSet<>());
        permission.getRoles().add(this);
    }

    public void removePermission(PermissionEntity permission) {
        if (permission == null) return;
        if (this.permissions != null) this.permissions.remove(permission);
        if (permission.getRoles() != null) permission.getRoles().remove(this);
    }

    public void addUser(UserEntity user) {
        if (user == null) return;
        if (this.users == null) this.users = new HashSet<>();
        this.users.add(user);
        if (user.getRoles() == null) user.setRoles(new HashSet<>());
        user.getRoles().add(this);
    }

    public void removeUser(UserEntity user) {
        if (user == null) return;
        if (this.users != null) this.users.remove(user);
        if (user.getRoles() != null) user.getRoles().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleEntity that = (RoleEntity) o;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
