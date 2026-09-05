package com.movieapp.userservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString(exclude = "author")
@EqualsAndHashCode(exclude = "author")
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String content;
    @ManyToOne
    Users author;

    public Post(String content,Users author) {
        this.content = content;
        this.author = author;
    }
}
