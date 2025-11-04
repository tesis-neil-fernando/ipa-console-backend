package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class CreateResponse {
    private boolean ok;
    private Long id;

    public CreateResponse() {}

    public CreateResponse(boolean ok, Long id) {
        this.ok = ok;
        this.id = id;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
