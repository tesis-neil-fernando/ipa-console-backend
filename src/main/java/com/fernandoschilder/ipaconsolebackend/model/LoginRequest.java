package com.fernandoschilder.ipaconsolebackend.model;

import lombok.*;

@Getter @Setter
public class LoginRequest {

    private String username;
    private String password;
}