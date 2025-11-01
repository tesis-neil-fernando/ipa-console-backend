package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class UpdateProcessRequest {
    private String name;
    private String description;

    public UpdateProcessRequest() {}

    public UpdateProcessRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
