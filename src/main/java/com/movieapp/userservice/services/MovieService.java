package com.movieapp.userservice.services;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.movieapp.userservice.exceptions.MovieAlreadyExistsException;
import com.movieapp.userservice.exceptions.MovieNotFoundException;
import com.movieapp.userservice.models.Genre;
import com.movieapp.userservice.models.Movie;
import com.movieapp.userservice.repositories.MovieRepository;

@Slf4j
@Service
public class MovieService {
    private RedisTemplate<String,Object> redisTemplate;
    private final MovieRepository repo;
    private static final String NOT_FOUND = "NOT_FOUND";

    public MovieService(MovieRepository repo,RedisTemplate<String,Object> redisTemplate) {
        this.repo = repo;
        this.redisTemplate = redisTemplate;
    }

    @Cacheable(cacheNames = "allMovies")
    @Transactional
    public List<Movie> getAllMovies() {
        log.info("Fetching Movies ");
        return repo.findAll();
    }

    @Cacheable(cacheNames = "moviesByGenre" , key="#genre")
    @Transactional
    public List<Movie> getMoviesByGenre(Genre genre)  {

        log.info("Current Thread {}",Thread.currentThread().getName());
        int maxRetry = 5;
        while(maxRetry > 0) {
            String cacheKey = "moviesByGenre::" + genre;
            String lockKey = "lock:moviesByGenre::" + genre;
            Object value = redisTemplate.opsForValue().get(cacheKey);

            if (value != null) {
                return (List<Movie>) value;
            }
            // Handle Cache Stampede
            String token = UUID.randomUUID().toString();
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey,token, Duration.ofSeconds(15));

            if(Boolean.TRUE.equals(acquired)) {
                log.info("DB Call Lock Acquired By {}",Thread.currentThread().getName());
                try {
//                    Thread.sleep(17000);
                    List<Movie> result = repo.getMoviesByGenre(genre);
                    redisTemplate.opsForValue().set(cacheKey,result);
                }
                finally {
                    releaseLock(lockKey,token);
                }

            }
            else {
                // Poll requests check redis if the value has been set or not
                log.debug("Failed To acquire Lock Waiting For Redis result");
                long start = System.currentTimeMillis();
                while(System.currentTimeMillis() - start < 5000) {
                    Object cached =  redisTemplate.opsForValue().get(cacheKey);
                    if(cached != null) return (List<Movie>) cached;

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
            maxRetry--;
        }
        throw new IllegalStateException("Cache population timeout");
    }

    private void releaseLock(String lockKey,String token) {
        String script = """
                if redis.call('GET',KEYS[1]) == ARGV[1] then
                   return redis.call('DEL',KEYS[1])
                else 
                   return 0
                end
                """;
        long res = redisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Collections.singletonList(lockKey),token);
        if(res == 1) log.info("Lock Released Successfully");
        else log.info("Failed to Delete lock");
    }

//    @Cacheable(cacheNames = "movies", key = "#id")
    public Movie getMovieById(Integer id) {
        log.info("Cache Miss");
        log.info("Current Thread {}",Thread.currentThread().getName());
        int maxRetry = 5;
        while(maxRetry > 0) {
            String cacheKey = "movies::" + id;
            String lockKey = "lock:movies::" + id;
            Object value = redisTemplate.opsForValue().get(cacheKey);

            if(value != null && value.equals(NOT_FOUND)) {
                throw new MovieNotFoundException(id);
            }

            if (value != null) {
                log.info("Fetched Cached result successfully");
                return (Movie) value;
            }
            // Handle Cache Stampede
            String token = UUID.randomUUID().toString();
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey,token, Duration.ofSeconds(15));

            if(Boolean.TRUE.equals(acquired)) {
                log.info("DB Call Lock Acquired By {}",Thread.currentThread().getName());
                try {
//                    Thread.sleep(17000);
                    Optional<Movie> result = repo.findById(id);
                    if(result.isEmpty()) {
                        redisTemplate.opsForValue().set(cacheKey,NOT_FOUND,Duration.ofSeconds(30));
                        throw new MovieNotFoundException(id);
                    }
                    redisTemplate.opsForValue().set(cacheKey,result.get());
                    return result.get();

                }
                finally {
                    releaseLock(lockKey,token);
                }

            }
            else {
                // Poll requests check redis if the value has been set or not
                log.debug("Failed To acquire Lock Waiting For Redis result");
                long start = System.currentTimeMillis();
                while(System.currentTimeMillis() - start < 5000) {
                    Object cached =  redisTemplate.opsForValue().get(cacheKey);
                    if(cached != null && cached.equals(NOT_FOUND)) {
                        throw new MovieNotFoundException(id);
                    }

                    if (cached != null) {
                        log.info("Fetched Cached result successfully");
                        return (Movie) cached;
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
            maxRetry--;
        }
        throw new IllegalStateException("Cache population timeout");
    }

    @CachePut(cacheNames = "movies", key = "#result.id")
    @Caching(evict = {
            @CacheEvict(cacheNames = "allMovies",allEntries = true),
            @CacheEvict(cacheNames = "moviesByGenre", allEntries = true)
    })
    @Transactional
    public Movie createMovie(Movie movie) {
        if (movie.getId() == null) {
            throw new IllegalArgumentException("Movie id must be provided");
        }

        if (repo.existsById(movie.getId())) {
            throw new MovieAlreadyExistsException(movie.getId());
        }

        if (repo.existsByTitle(movie.getTitle()) && repo.existsByDirector(movie.getDirector())) {
            throw new MovieAlreadyExistsException(movie.getId());
        }

        return repo.save(movie);
    }

    @CachePut(cacheNames = "movies", key = "#id")
    @Caching(evict = {
            @CacheEvict(cacheNames = "allMovies",allEntries = true),
            @CacheEvict(cacheNames = "moviesByGenre", allEntries = true)
    })
    @Transactional
    public Movie updateMovie(Integer id, Movie updatedMovie) {
        Movie existingMovie = repo.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        existingMovie.setTitle(updatedMovie.getTitle());
        existingMovie.setYear(updatedMovie.getYear());
        existingMovie.setGenre(updatedMovie.getGenre());
        existingMovie.setRating(updatedMovie.getRating());
        existingMovie.setVotes(updatedMovie.getVotes());
        existingMovie.setDirector(updatedMovie.getDirector());
        existingMovie.setActor1(updatedMovie.getActor1());
        existingMovie.setActor2(updatedMovie.getActor2());
        existingMovie.setDurationMin(updatedMovie.getDurationMin());
        existingMovie.setBudgetMillion(updatedMovie.getBudgetMillion());
        existingMovie.setRevenueMillion(updatedMovie.getRevenueMillion());

        Movie updated = repo.save(existingMovie);
        if(true) throw new RuntimeException("Deliberate exception");
        return updated;
    }

    @CacheEvict(cacheNames = "movies", key = "#id")
    @Caching(evict = {
            @CacheEvict(cacheNames = "allMovies",allEntries = true),
            @CacheEvict(cacheNames = "moviesByGenre", allEntries = true)
    })
    @Transactional
    public void deleteMovie(Integer id) {
        Movie movie = repo.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        repo.delete(movie);
    }

    // Cache Avalanche
    public void warmMovies() {
        String key = "movies::";
        long ttl = 20 + ThreadLocalRandom.current().nextLong(0, 11);
        for(int i=0;i<5;i++) {
            try {
                Optional<Movie> m = repo.findById(i);
                if(m.isEmpty()) continue;
                redisTemplate.opsForValue().set(key + i,m.get(),Duration.ofSeconds(ttl));
            }
            catch(Exception e) {
                // handle exception
            }

        }
    }
}