package com.fernandoschilder.ipaconsolebackend.dto.rbac;

import java.util.List;

public class UserRbacDto {
    private Long id;
    private String username;
    private String name;
    private Boolean enabled;
    private List<RoleRefDto> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public List<RoleRefDto> getRoles() { return roles; }
    public void setRoles(List<RoleRefDto> roles) { this.roles = roles; }
}
