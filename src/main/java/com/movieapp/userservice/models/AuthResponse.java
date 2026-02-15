package com.movieapp.userservice.models;

import java.util.List;
import java.util.UUID;

public class AuthResponse {
	
	String token;
	List<String> roles;
	UUID userId;
	
	public AuthResponse() {}
	
	public AuthResponse(UUID userId,String token,List<String> role) {
		this.userId = userId;
		this.token = token;
		this.roles = role;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}
		
}
