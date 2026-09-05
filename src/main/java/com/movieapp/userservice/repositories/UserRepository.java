package com.movieapp.userservice.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.movieapp.userservice.models.Users;
import org.springframework.data.jpa.repository.EntityGraph;

public interface UserRepository extends JpaRepository<Users,UUID> {
	@EntityGraph(attributePaths = "roles")
	public Optional<Users> findByUsername(String username);
	@EntityGraph(attributePaths = "roles")
	public Optional<Users> findByUsernameOrEmail(String username, String email);
	@EntityGraph(attributePaths = "roles")
	public Optional<Users> findById(UUID id);
	
	

	@EntityGraph(attributePaths = "roles")
	public Optional<Users> findByEmail(String email);
	
	boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);
}
