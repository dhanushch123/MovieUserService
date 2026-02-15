package com.movieapp.userservice.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movieapp.userservice.models.Users;

public interface UserRepository extends JpaRepository<Users,UUID> {
	
	public Optional<Users> findByUsername(String username);
	
	public Optional<Users> findByUsernameOrEmail(String username, String email);
	
	public Optional<Users> findById(UUID id);
	
	public Optional<Users> findByEmail(String email);
	
	boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);
}
