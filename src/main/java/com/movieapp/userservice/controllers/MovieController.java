package com.movieapp.userservice.controllers;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movieapp.userservice.models.Genre;
import com.movieapp.userservice.models.Movie;
import com.movieapp.userservice.services.MovieService;

@Slf4j
@RestController
@RequestMapping("/public/movie")
public class MovieController {
	
	private final MovieService service;
	
	public MovieController(MovieService service) {
		this.service = service;
	}
	
	@GetMapping
	public ResponseEntity<List<Movie>> getAllMovies(
			@RequestParam(required = false) Genre genre) {

		List<Movie> movies = genre == null
				? service.getAllMovies()
				: service.getMoviesByGenre(genre);

		return ResponseEntity.ok(movies);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Movie> getMovieById(@PathVariable Integer id) {
		log.info("Get By Id Method Invoked");
		return ResponseEntity.ok(service.getMovieById(id));
	}

	// Keeps the original endpoint available while clients migrate to GET /public/movie?genre=...
	@GetMapping("/movies")
	public ResponseEntity<List<Movie>> getMoviesByGenre(@RequestParam Genre genre) {
		return ResponseEntity.ok(service.getMoviesByGenre(genre));
	}

	@PostMapping
	public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
		Movie createdMovie = service.createMovie(movie);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Movie> updateMovie(
			@PathVariable Integer id,
			@RequestBody Movie movie) {
		return ResponseEntity.ok(service.updateMovie(id, movie));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMovie(@PathVariable Integer id) {
		service.deleteMovie(id);
		return ResponseEntity.noContent().build();
	}
}
