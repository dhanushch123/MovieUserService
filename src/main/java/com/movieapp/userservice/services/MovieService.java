package com.movieapp.userservice.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.movieapp.userservice.models.Genre;
import com.movieapp.userservice.models.Movie;
import com.movieapp.userservice.repositories.MovieRepository;

@Service
public class MovieService {
	
	MovieRepository repo;
	
	public MovieService(MovieRepository repo) {
		this.repo = repo;
	}
	
	public List<Movie> getAllMovies() {
		return repo.findAll();
	}
	
	public List<Movie> getMoviesByGenre(Genre genre) {
		return repo.getMoviesByGenre(genre);
	}
	
	public Movie getMovieById(Integer id) {
		Movie movie = repo.findById(id).get();
		if(movie == null) throw new RuntimeException("User Not Found");
		return movie;
	}
}
