package com.movieapp.userservice.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String title;

    private Integer year;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating;

    private Integer votes;

    private String director;

    @Column(name = "actor_1")
    private String actor1;

    @Column(name = "actor_2")
    private String actor2;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "budget_million", precision = 10, scale = 1)
    private BigDecimal budgetMillion;

    @Column(name = "revenue_million", precision = 10, scale = 1)
    private BigDecimal revenueMillion;

    // 🔹 Constructors
    public Movie() {}

    public Movie(Integer id, String title, Integer year, Genre genre,
                 BigDecimal rating, Integer votes, String director,
                 String actor1, String actor2, Integer durationMin,
                 BigDecimal budgetMillion, BigDecimal revenueMillion) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.rating = rating;
        this.votes = votes;
        this.director = director;
        this.actor1 = actor1;
        this.actor2 = actor2;
        this.durationMin = durationMin;
        this.budgetMillion = budgetMillion;
        this.revenueMillion = revenueMillion;
    }

    // 🔹 Getters & Setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getVotes() { return votes; }
    public void setVotes(Integer votes) { this.votes = votes; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getActor1() { return actor1; }
    public void setActor1(String actor1) { this.actor1 = actor1; }

    public String getActor2() { return actor2; }
    public void setActor2(String actor2) { this.actor2 = actor2; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public BigDecimal getBudgetMillion() { return budgetMillion; }
    public void setBudgetMillion(BigDecimal budgetMillion) { this.budgetMillion = budgetMillion; }

    public BigDecimal getRevenueMillion() { return revenueMillion; }
    public void setRevenueMillion(BigDecimal revenueMillion) { this.revenueMillion = revenueMillion; }
}