package com.movieapp.userservice.services;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.movieapp.userservice.models.AuthResponse;
import com.movieapp.userservice.models.UserPrincipal;
import com.movieapp.userservice.models.Users;
import com.movieapp.userservice.repositories.UserRepository;


@Service
public class UserService {
	
	UserRepository repo;
	PasswordEncoder encoder;
	AuthenticationManager authManager;
	JWTService jwtService;
	
	public UserService(UserRepository repo,PasswordEncoder encoder,AuthenticationManager authManager,JWTService jwtService) {
		this.repo = repo;
		this.encoder = encoder;
		this.authManager = authManager;
		this.jwtService = jwtService;
	}
	
	public void registerUser(Users user) {
		System.out.println(user.getFirstName() + " " + user.getLastName());
		if(repo.existsByEmail(user.getEmail()) || repo.existsByMobile(user.getMobile())) {
			// User already exists with this email or mobile
			throw new RuntimeException("User already exists with same email or mobile");
		}
		user.setPassword(encoder.encode(user.getPassword()));
		user.setUsername(user.getFirstName()+user.getMobile().substring(4,9));;
		repo.save(user);
	}
	

	public AuthResponse authenticate(String username, String password) {
		// User may enter username or email
		System.out.println(username + " " +password);
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,password);
		Authentication authentication = authManager.authenticate(authToken);
		if(authentication.isAuthenticated()) {
			Users user = repo.findByUsernameOrEmail(username,username).get();
			return getAuthResponse(user);
		}
		return null;
	}
	
	public AuthResponse getAuthResponse(Users user) {
		String token = jwtService.generateToken(new UserPrincipal(user));
		return new AuthResponse(user.getId(),token,user.getRoles().stream().map(role->role.name()).toList());
	}

	public Users findByEmail(String email) {
		return repo.findByEmail(email).get();
	}
	
	
}
