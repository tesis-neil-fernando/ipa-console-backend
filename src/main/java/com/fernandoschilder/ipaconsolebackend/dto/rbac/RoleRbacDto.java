package com.fernandoschilder.ipaconsolebackend.dto.rbac;

import java.util.List;

public class RoleRbacDto {
    private Long id;
    private String name;
    private List<UserRefDto> users;
    private List<PermissionRbacDto> permissions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<UserRefDto> getUsers() { return users; }
    public void setUsers(List<UserRefDto> users) { this.users = users; }
    public List<PermissionRbacDto> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionRbacDto> permissions) { this.permissions = permissions; }
}
