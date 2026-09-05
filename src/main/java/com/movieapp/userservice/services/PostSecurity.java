package com.movieapp.userservice.services;

import com.movieapp.userservice.models.Post;
import com.movieapp.userservice.models.UserPrincipal;
import com.movieapp.userservice.models.Users;
import com.movieapp.userservice.repositories.PostRepository;
import com.movieapp.userservice.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PostSecurity {

    private final PostRepository postRepo;

    public PostSecurity(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    public boolean isAuthor(long postId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        return postRepo.existsByIdAndAuthorUsername(
                postId,
                principal.getUsername()
        );
    }
}
