package com.fernandoschilder.ipaconsolebackend.dto.rbac;

import java.util.List;

public class UserRbacDto {
    private Long id;
    private String username;
    private List<RoleRefDto> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<RoleRefDto> getRoles() { return roles; }
    public void setRoles(List<RoleRefDto> roles) { this.roles = roles; }
}
