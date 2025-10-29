package com.fernandoschilder.ipaconsolebackend.dto;

/** Simple login request as a record (no Lombok). */
public record LoginRequest(String username, String password) {}