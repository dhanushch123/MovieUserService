package com.movieapp.userservice.controllers;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movieapp.userservice.models.UserDTO;


@RestController
@RequestMapping("/public")
public class PublicController {

	@PostMapping
	public String greet(@RequestBody UserDTO user) {
		return "Hello" + user.getUsername();
	}
}
