package com.movieapp.userservice.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movieapp.userservice.models.Genre;
import com.movieapp.userservice.models.Movie;
import com.movieapp.userservice.services.MovieService;

@RestController
@RequestMapping("/public/movie")
public class MovieController {
	
	MovieService service;
	
	public MovieController(MovieService service) {
		this.service = service;
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getMovieById(@PathVariable Integer id) {
		Movie movie = null;
		try {
			movie = service.getMovieById(id);
		}
		catch(Exception e) {
			Map<String,String> map = new HashMap<>();
			map.put("message",e.getLocalizedMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
		}
		return ResponseEntity.ok(movie);
	}
	
	@GetMapping("/movies")
	public ResponseEntity<?> getMoviesByGenre(@RequestParam Genre genre) {
		List<Movie> movies = null;
		try {
			movies = service.getMoviesByGenre(genre);
		}
		catch(Exception e) {
			Map<String,String> map = new HashMap<>();
			map.put("message",e.getLocalizedMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
		}
		return ResponseEntity.ok(movies);
		
		
	}
}
