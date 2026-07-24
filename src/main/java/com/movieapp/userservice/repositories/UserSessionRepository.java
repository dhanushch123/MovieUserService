package com.movieapp.userservice.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.movieapp.userservice.models.UserSession;

import jakarta.transaction.Transactional;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
	
	@Modifying
	@Transactional
	@Query(""" 
			UPDATE UserSession s
			SET s.revoked = true
			WHERE s.expiresAt <= CURRENT_TIMESTAMP AND s.revoked = false
			""")
	public void revokeExpiredSessions();
	
	@Modifying
	@Transactional
	@Query(value = """ 
			WITH sessions AS (
			SELECT id,
			ROW_NUMBER() OVER (ORDER BY expires_at DESC) AS rn
			FROM user_session WHERE user_id = :userId AND revoked = false)
			UPDATE user_session SET revoked = true 
			WHERE id IN (SELECT id FROM sessions WHERE rn > 5) 
			""",nativeQuery=true)
	public void limitSessions(@Param("userId") long userId);
	
	@Query(""" 
			SELECT s FROM UserSession s WHERE s.refreshToken=:token
			""")
	public UserSession findByRefreshToken(@Param("token")String encodedToken);
}
