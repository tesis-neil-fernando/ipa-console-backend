package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class CreateUserRequest {
	private String username;
	private String password;
	private String name;

	public CreateUserRequest() {}

	public CreateUserRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public CreateUserRequest(String username, String name, String password) {
		this.username = username;
		this.name = name;
		this.password = password;
	}

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
}
