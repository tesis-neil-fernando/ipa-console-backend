package com.fernandoschilder.ipaconsolebackend.dto.rbac;

import java.util.List;

public class NamespaceRbacDto {
    private Long id;
    private String name;
    private List<PermissionRbacDto> permissions;
    private List<ProcessRbacDto> processes;
    private List<RoleRefDto> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<PermissionRbacDto> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionRbacDto> permissions) { this.permissions = permissions; }
    public List<ProcessRbacDto> getProcesses() { return processes; }
    public void setProcesses(List<ProcessRbacDto> processes) { this.processes = processes; }
    public List<RoleRefDto> getRoles() { return roles; }
    public void setRoles(List<RoleRefDto> roles) { this.roles = roles; }
}
