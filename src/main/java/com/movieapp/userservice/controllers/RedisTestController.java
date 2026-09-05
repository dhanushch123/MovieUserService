package com.movieapp.userservice.controllers;

import com.movieapp.userservice.models.Genre;
import com.movieapp.userservice.models.Movie;
import com.movieapp.userservice.services.MovieService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisTestController {


    private final RedisTemplate<String, Object> redisTemplate;
    private MovieService movieService;

    public RedisTestController(RedisTemplate<String, Object> redisTemplate,MovieService movieService) {
        this.redisTemplate = redisTemplate;
        this.movieService = movieService;
    }

    @GetMapping("/test")
    public Movie test() {
        Movie m = new Movie();
        m.setTitle("Fauzi");
        m.setDirector("Hanu Cameron");
        m.setGenre(Genre.DRAMA);
        m.setYear(2026);
        m.setActor1("India's Biggest Super Star Prabhas");
        redisTemplate.opsForValue().set("test:name", m);

        return (Movie) redisTemplate.opsForValue().get("test:name");
    }

    @PostMapping("warmMovies")
    public String warmMovies() {
        movieService.warmMovies();
        return "cached Movies";
    }
}
