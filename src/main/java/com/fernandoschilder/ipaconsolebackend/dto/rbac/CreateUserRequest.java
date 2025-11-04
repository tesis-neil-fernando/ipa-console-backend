package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class CreateUserRequest {
	private String username;
	private String name;

	public CreateUserRequest() {}

	public CreateUserRequest(String username) {
		this.username = username;
	}

	public CreateUserRequest(String username, String name) {
		this.username = username;
		this.name = name;
	}

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
}
