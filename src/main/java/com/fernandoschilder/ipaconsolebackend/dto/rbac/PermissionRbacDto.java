package com.fernandoschilder.ipaconsolebackend.dto.rbac;

import java.util.List;

public class PermissionRbacDto {
    private Long id;
    private String type;
    private List<NamespaceRefDto> namespaces;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<NamespaceRefDto> getNamespaces() { return namespaces; }
    public void setNamespaces(List<NamespaceRefDto> namespaces) { this.namespaces = namespaces; }
}
