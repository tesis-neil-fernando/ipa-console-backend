package com.fernandoschilder.ipaconsolebackend.dto;

import lombok.*;

@Getter @Setter
public class LoginRequest {

    private String username;
    private String password;
}