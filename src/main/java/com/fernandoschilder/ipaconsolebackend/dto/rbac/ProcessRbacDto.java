package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class ProcessRbacDto {
    private Long id;
    private String name;
    private Long namespaceId;
    private String namespaceName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getNamespaceId() { return namespaceId; }
    public void setNamespaceId(Long namespaceId) { this.namespaceId = namespaceId; }
    public String getNamespaceName() { return namespaceName; }
    public void setNamespaceName(String namespaceName) { this.namespaceName = namespaceName; }
}
