package com.movieapp.userservice.controllers;

import com.movieapp.userservice.models.Post;
import com.movieapp.userservice.models.PostDto;
import com.movieapp.userservice.models.UserPrincipal;
import com.movieapp.userservice.models.Users;
import com.movieapp.userservice.repositories.PostRepository;
import com.movieapp.userservice.repositories.UserRepository;
import com.movieapp.userservice.services.PostSecurity;
import com.movieapp.userservice.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/posts")
public class PostController {

    private final PostSecurity postSecurity;
    private final UserService userService;
    private PostRepository repo;
    private UserRepository userRepo;

    public PostController(PostRepository repo, PostSecurity postSecurity, UserService userService) {
        this.repo = repo;
        this.postSecurity = postSecurity;
        this.userService = userService;
    }



    @GetMapping("/{id}")
    @PreAuthorize("@postSecurity.isAuthor(#id,authentication)")
    public ResponseEntity<PostDto> getPost(@PathVariable long id) {
        Post post = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException());
        System.out.println("Fetched post");
        return ResponseEntity.ok(new PostDto(post.getContent(),post.getAuthor().getEmail()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PostDto> createPost(
            @RequestBody PostDto request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Add a findByUsername method in UserService.
        Users author = userService.findByUsername(principal.getUsername());

        Post savedPost = repo.save(new Post(request.getContent(), author));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PostDto(author.getEmail(), savedPost.getContent()));
    }

    public boolean isAuthor(long id) {
        return repo.existsById(id);
    }
}
