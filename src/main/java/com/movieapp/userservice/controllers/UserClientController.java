package com.movieapp.userservice.controllers;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.movieapp.userservice.models.UserClientDTO;



@RestController
@RequestMapping("public/api/v1/users")
public class UserClientController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserClientController.class);

    private final RestClient restClient;

    public UserClientController(RestClient restClient) {
        this.restClient = restClient;
    }

    @GetMapping
    public List<UserClientDTO> getAll() {
    	logger.info("Received request to fetch all users");
    	List<UserClientDTO> users = restClient.get()
                .uri("/api/users")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserClientDTO>>() {});
        logger.info("Fetched {} users", users.size());
        logger.debug("Users: {}", users);
        
        return users;
    }

    @GetMapping("/{id}")
    public UserClientDTO getById(@PathVariable Long id) {
    	logger.info("Fetching user with id {}", id);
    	UserClientDTO user = restClient.get()
                .uri("/api/users/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 500,
                		(request, response) -> {
                			logger.error("Remote service returned 500 while fetching user {}", id);
                            throw new RuntimeException("Internal Server Error");
                        })
                .body(UserClientDTO.class);
    	
    	logger.info("Successfully fetched user {}", id);

        logger.debug("User details: {}", user);
    	
    	return user;
    }

    @PostMapping
    public UserClientDTO create(@RequestBody UserClientDTO dto) {

        logger.info("Creating user");

        logger.debug("Request Body: {}", dto);

        UserClientDTO createdUser = restClient.post()
                .uri("/api/users")
                .body(dto)
                .retrieve()
                .body(UserClientDTO.class);

        logger.info("User created successfully with id {}", createdUser.getId());

        return createdUser;
    }

    @PutMapping("/{id}")
    public UserClientDTO update(@PathVariable Long id,
                                @RequestBody UserClientDTO dto) {

        logger.info("Updating user {}", id);

        logger.debug("Updated DTO: {}", dto);

        UserClientDTO updatedUser = restClient.put()
                .uri("/api/users/{id}", id)
                .body(dto)
                .retrieve()
                .body(UserClientDTO.class);

        logger.info("User {} updated successfully", id);

        return updatedUser;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        logger.info("Deleting user {}", id);

        restClient.delete()
                .uri("/api/users/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 500,
        		(request, response) -> {
        			logger.error("Remote service returned 500 while fetching user {}", id);
                    throw new RuntimeException("Internal Server Error");
                })
                .toBodilessEntity();

        logger.info("User {} deleted successfully", id);
    }
}