package com.movieapp.userservice.repositories;

import com.movieapp.userservice.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    boolean existsByIdAndAuthorUsername(Long id, String username);
}
