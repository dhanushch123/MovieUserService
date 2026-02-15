package com.movieapp.userservice.controllers;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@EnableMethodSecurity
public class UserController {
	
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public String greet() {
		return "Hi User welcome !!";
	}
}
