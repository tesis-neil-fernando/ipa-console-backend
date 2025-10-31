package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class CreateNamespaceRequest {
    private String name;

    public CreateNamespaceRequest() {}

    public CreateNamespaceRequest(String name) { this.name = name; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
