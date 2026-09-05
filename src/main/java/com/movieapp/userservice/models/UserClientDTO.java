package com.movieapp.userservice.models;

import java.time.LocalDateTime;

public class UserClientDTO {
    private Long id;
    private String name;
    private String email;
    private String country;
    private LocalDateTime createdAt;

    public UserClientDTO() {}

    public UserClientDTO(Long id, String name, String email, String country, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.country = country;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

