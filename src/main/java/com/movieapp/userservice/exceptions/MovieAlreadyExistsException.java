package com.movieapp.userservice.exceptions;

public class MovieAlreadyExistsException extends RuntimeException {

    public MovieAlreadyExistsException(Integer id) {
        super("Movie already exists with id: " + id);
    }
}
