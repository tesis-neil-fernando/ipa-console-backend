package com.fernandoschilder.ipaconsolebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Simple login request as a record (no Lombok).
 *  Password is marked write-only so it won't be serialized in responses or logs by Jackson.
 *  Validation annotations protect against empty or unexpectedly large inputs.
 */
public record LoginRequest(
	@NotBlank(message = "username must not be blank")
	@Size(max = 150, message = "username must be at most 150 characters")
	String username,

	@NotBlank(message = "password must not be blank")
	@Size(min = 6, max = 256, message = "password must be between 6 and 256 characters")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	String password) {
}