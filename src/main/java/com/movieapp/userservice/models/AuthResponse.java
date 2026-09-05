package com.movieapp.userservice.models;

import java.util.List;
import java.util.UUID;

public class AuthResponse {
	
	String accessToken;
	List<String> roles;
	UUID userId;
	
	public AuthResponse() {}
	
	public AuthResponse(UUID userId,String accessToken,List<String> role) {
		this.userId = userId;
		this.accessToken = accessToken;
		this.roles = role;
	}

	

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
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
