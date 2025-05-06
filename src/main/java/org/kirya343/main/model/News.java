package org.kirya343.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "news")
public class News {
    // Геттеры и сеттеры
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String excerpt;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private LocalDateTime publishDate;

    @Column(nullable = false)
    private boolean published;

}