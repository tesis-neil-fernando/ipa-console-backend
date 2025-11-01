package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class UpdateUserEnabledRequest {
    private Boolean enabled;

    public UpdateUserEnabledRequest() {}

    public UpdateUserEnabledRequest(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
