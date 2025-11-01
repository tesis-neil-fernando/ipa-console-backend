package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class UpdateNameRequest {
    private String name;

    public UpdateNameRequest() {}

    public UpdateNameRequest(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
