package com.fernandoschilder.ipaconsolebackend.dto.rbac;

public class UpdatePasswordRequest {
	private Long id;
	private String password;

	public UpdatePasswordRequest() {}

	public UpdatePasswordRequest(Long id, String password) {
		this.id = id;
		this.password = password;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
}
