package com.movieapp.userservice.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movieapp.userservice.models.AuthResponse;
import com.movieapp.userservice.models.UserDTO;
import com.movieapp.userservice.models.Users;
import com.movieapp.userservice.services.JWTService;
import com.movieapp.userservice.services.TempCodeService;
import com.movieapp.userservice.services.UserService;



@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
	
	UserService userService;
	TempCodeService tempCodeService;
	JWTService jwtService;
	
	public AuthenticationController(UserService userService,TempCodeService tempCodeService,JWTService jwtService) {
		this.userService = userService;
		this.tempCodeService = tempCodeService;
		this.jwtService = jwtService;
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDTO user) {
		AuthResponse authResponse = null;
		System.out.println(user.getUsername() + user.getPassword());
		authResponse = userService.authenticate(user.getUsername(),user.getPassword());
		return ResponseEntity.ok(authResponse);
		
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody Users user){
		userService.registerUser(user);
		Map<String,String> map = new HashMap<>();
		map.put("message", "User created successfully");
		return ResponseEntity.status(HttpStatus.CREATED).body(map);
	}
	
	@GetMapping("/token")
	public ResponseEntity<?> getToken(@RequestParam String loginCode) {
		String email = tempCodeService.consumeCode(loginCode);
		if(email != null) {
			Users user = userService.findByEmail(email);
			AuthResponse authResponse = userService.getAuthResponse(user);
			return ResponseEntity.ok(authResponse);
		}
		Map<String,String> map = new HashMap<>();
		map.put("message","Login code Expired");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	}
	
}
