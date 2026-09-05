package com.movieapp.userservice.controllers;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Tag(
	    name = "Authentication",
	    description = "Endpoints responsible for user authentication, registration, and token management using JWT-based security."
	)
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
	
	@Operation(
	        summary = "Authenticate user",
	        description = "Authenticates a user using username and password. Returns a JWT access token upon successful authentication."
	    )
	    @ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Authentication successful",
	            content = @Content(mediaType = "application/json",
	                schema = @Schema(implementation = AuthResponse.class))),
	        @ApiResponse(responseCode = "401", description = "Invalid credentials",
	            content = @Content),
	        @ApiResponse(responseCode = "500", description = "Internal server error",
	            content = @Content)
	    })
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDTO user,HttpServletRequest request,HttpServletResponse response) throws NoSuchAlgorithmException {
		AuthResponse authResponse = null;
		authResponse = userService.authenticate(user.getUsername(),user.getPassword(),request,response);
		return ResponseEntity.ok(authResponse);
		
	}
	
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account in the system."
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody Users user){
		userService.registerUser(user);
		Map<String,String> map = new HashMap<>();
		map.put("message", "User created successfully");
		return ResponseEntity.status(HttpStatus.CREATED).body(map);
	}
	
	@Operation(
	        summary = "Exchange login code for JWT token",
	        description = "Consumes a temporary login code and returns a JWT token if valid."
	    )
	    @ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Token generated successfully",
	            content = @Content(mediaType = "application/json",
	                schema = @Schema(implementation = AuthResponse.class))),
	        @ApiResponse(responseCode = "400", description = "Invalid or expired login code"),
	        @ApiResponse(responseCode = "500", description = "Internal server error")
	    })
	@GetMapping("/token")
	public ResponseEntity<?> getToken(@RequestParam String loginCode,HttpServletRequest request,HttpServletResponse response) throws NoSuchAlgorithmException {
		String email = tempCodeService.consumeCode(loginCode);
		if(email != null) {
			Users user = userService.findByEmail(email);
			AuthResponse authResponse = userService.getAuthResponse(user,request,response);
			return ResponseEntity.ok(authResponse);
		}
		Map<String,String> map = new HashMap<>();
		map.put("message","Login code Expired");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	}
	
	@Operation(
		    summary = "Refresh access token",
		    description = "Validates the refresh token from cookies and issues a new JWT access token."
		)
		@ApiResponses(value = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Access token refreshed successfully",
		        content = @Content(
		            mediaType = "application/json",
		            schema = @Schema(implementation = AuthResponse.class)
		        )
		    ),
		    @ApiResponse(
		        responseCode = "401",
		        description = "Invalid, expired, or missing refresh token"
		    ),
		    @ApiResponse(
		        responseCode = "500",
		        description = "Internal server error"
		    )
		})
	@PostMapping("/refresh")
	public ResponseEntity<?> getAccessToken(@CookieValue("RefreshToken") String refreshToken) {
		Map<String,String> map = new HashMap<>();
		AuthResponse authResponse = null;
		if(refreshToken == null) {
			map.put("message","No Refresh Token");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
		}
		try {
			authResponse = userService.refreshAccessToken(refreshToken);
		}
		catch(Exception e) {
			map.put("message","No Refresh Token");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
		}
		return ResponseEntity.ok(authResponse);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<?> logout(@CookieValue("RefreshToken")String refreshToken,HttpServletResponse response) {
		HashMap<String,String> map = new HashMap<>();
		
		try {
			userService.invalidateSession(refreshToken,response);
		}
		catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		map.put("message", "Logged out successfully");
		return ResponseEntity.ok(map);
		
	}
}
