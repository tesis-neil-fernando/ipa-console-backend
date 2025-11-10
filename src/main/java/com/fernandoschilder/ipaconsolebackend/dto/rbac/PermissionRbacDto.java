package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class PermissionRbacDto {
    private Long id;
    // action: one of "view", "exec", "edit"
    private String action;
    // namespace (null for global)
    private NamespaceRefDto namespace;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public NamespaceRefDto getNamespace() { return namespace; }
    public void setNamespace(NamespaceRefDto namespace) { this.namespace = namespace; }
}
