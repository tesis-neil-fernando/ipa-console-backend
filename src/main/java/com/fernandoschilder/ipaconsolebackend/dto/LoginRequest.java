package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Simple login request as a record (no Lombok).
 *  Password is marked write-only so it won't be serialized in responses or logs by Jackson.
 */
public record LoginRequest(String username,
						   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
						   String password) {
}