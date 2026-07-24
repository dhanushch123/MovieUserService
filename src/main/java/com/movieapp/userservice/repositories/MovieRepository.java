package com.movieapp.userservice.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.movieapp.userservice.models.Genre;
import com.movieapp.userservice.models.Movie;


public interface MovieRepository extends JpaRepository<Movie,Integer> {
	
	
	
	@Query("SELECT m FROM Movie m WHERE m.genre = :genre")
	List<Movie> getMoviesByGenre(@Param("genre") Genre genre);
	
	
}
