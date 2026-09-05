package com.movieapp.userservice.controllers;


import com.movieapp.userservice.TestConfiguration;
import com.movieapp.userservice.configurations.TestContainersConfig;
import com.movieapp.userservice.models.Genre;
import com.movieapp.userservice.models.Movie;
import com.movieapp.userservice.repositories.MovieRepository;
import org.apache.tomcat.util.http.parser.MediaType;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.TimeZone;

import static org.springframework.http.MediaType.APPLICATION_JSON;


public class MovieControllerIntegrationTest extends TestConfiguration {



    @Autowired
    private MovieRepository movieRepository;

    @BeforeEach
    void cleanDatabase() {
        movieRepository.deleteAll();
    }





    @Test
    void getMovieById_shouldReturnSavedMovie() {
        // Arrange
        Movie movie = new Movie();
        movie.setId(1);
        movie.setTitle("Inception");
        movie.setYear(2010);
        movie.setGenre(Genre.SCI_FI);
        movie.setRating(new BigDecimal("8.8"));

        movieRepository.save(movie);

        // Act and Assert
        webTestClient.get()
                .uri("/public/movie/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Inception")
                .jsonPath("$.year").isEqualTo(2010)
                .jsonPath("$.genre").isEqualTo("SCI_FI")
                .jsonPath("$.rating").isEqualTo(8.8);
    }

    // Test Movie not found exception
    @Test
    void getMovieById_shouldReturn404_whenMovieDoesNotExist() {
        webTestClient.get()
                .uri("/public/movie/{id}",2)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("MOVIE_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Movie not found with id: 2");
    }

    @Test
    void getMoviesByGenre_shouldReturnMovies_whenGenreExists() {
        Movie movie1 = new Movie();
        movie1.setId(1);
        movie1.setTitle("SpiderMan Brand New");
        movie1.setYear(2026);
        movie1.setGenre(Genre.ACTION);
        movie1.setRating(new BigDecimal("9.5"));
        movieRepository.save(movie1);

        Movie movie2 = new Movie();
        movie2.setId(2);
        movie2.setTitle("Bahubali 2");
        movie2.setYear(2018);
        movie2.setGenre(Genre.ACTION);
        movie2.setRating(new BigDecimal("10"));
        movieRepository.save(movie2);

        Movie movie = new Movie();
        movie.setId(3);
        movie.setTitle("Inception");
        movie.setYear(2010);
        movie.setGenre(Genre.SCI_FI);
        movie.setRating(new BigDecimal("8.8"));

        movieRepository.save(movie);

        Genre genre = Genre.ACTION;
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/movie")
                        .queryParam("genre", genre)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBodyList(Movie.class)
                .hasSize(2)
                .value(movies -> {
                    assertThat(movies)
                            .allMatch(m -> m.getGenre() == Genre.ACTION);
                });

    }

    @Test
    void getMoviesByGenre_shouldReturnEmptyList_whenNoMoviesMatchGenre() {
        Genre genre = Genre.COMEDY;
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/movie")
                        .queryParam("genre",genre)
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBodyList(Movie.class)
                .hasSize(0);
    }

    @Test
    void getMoviesByGenre_shouldReturnBadRequest_whenGenreIsInvalid() {
        String genre = "ADVENTURE";
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public/movie")
                        .queryParam("genre",genre)
                        .build()
                )
                .exchange()
                .expectStatus().is4xxClientError();
        // Because cant map Adventure to Genre as it is not valid genre we have
    }

    @Test
    void createMovie_shouldCreateMovie_whenRequestIsValid() {
        Movie movie = new Movie();
        movie.setId(3);
        movie.setTitle("Inception");
        movie.setYear(2010);
        movie.setGenre(Genre.SCI_FI);
        movie.setRating(new BigDecimal("8.8"));
        webTestClient.post()
                .uri("/public/movie")
                .bodyValue(movie)
                .exchange()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.title").isEqualTo("Inception")
                .jsonPath("$.year").isEqualTo(2010)
                .jsonPath("$.genre").isEqualTo("SCI_FI")
                .jsonPath("$.rating").isEqualTo(8.8);
    }

    @Test
    void createMovie_shouldReturnBadRequest_whenRequestIsInvalid() {
        Movie movie = new Movie();
        movie.setId(3);
        movie.setTitle("Inception");
        movie.setYear(2010);
        movie.setDirector("Nolan");
        movie.setGenre(Genre.SCI_FI);
        movie.setRating(new BigDecimal("8.8"));
        movieRepository.save(movie);

        Movie newMovie = new Movie();
        newMovie.setId(3);
        newMovie.setTitle("Inception");
        newMovie.setDirector("Nolan");
        newMovie.setYear(2010);
        newMovie.setGenre(Genre.SCI_FI);
        newMovie.setRating(new BigDecimal("8.5"));

        webTestClient.post()
                .uri("/public/movie")
                .bodyValue(newMovie)
                .exchange()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.error").isEqualTo("MOVIE_ALREADY_EXISTS");

    }

    void deleteMovieById_shouldDeleteMovie_whenExistsInDB() {
        Movie newMovie = new Movie();
        newMovie.setId(3);
        newMovie.setTitle("Inception");
        newMovie.setDirector("Nolan");
        newMovie.setYear(2010);
        newMovie.setGenre(Genre.SCI_FI);
        newMovie.setRating(new BigDecimal("8.5"));
        movieRepository.save(newMovie);
        int id = 3;
        webTestClient.delete()
                .uri("/public/movie/{id}",id)
                .exchange()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_JSON)
                .expectStatus().isNoContent();

    }

    void deleteMovieById_shouldThrow404_whenMovieDoesNotExist() {
        int id = 2;
        webTestClient.delete()
                .uri("/public/movie/{id}",id)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("MOVIE_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Movie not found with id: 2");
    }







}


