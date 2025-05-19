package org.kirya343.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Setter
@Getter
@Entity
@Table(name = "news")
public class News {
    // Геттеры и сеттеры
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Заголовки на разных языках
    @Column(nullable = false)
    private String titleRu;
    @Column(nullable = false)
    private String titleFi;
    @Column(nullable = false)
    private String titleEn;

    // контент на разных языках
    @Column(length = 2000, columnDefinition = "TEXT")
    private String contentRu;
    @Column(length = 2000, columnDefinition = "TEXT")
    private String contentFi;
    @Column(length = 2000, columnDefinition = "TEXT")
    private String contentEn;

    // контент на разных языках
    @Column(length = 2000, columnDefinition = "TEXT")
    private String excerptRu;
    @Column(length = 2000, columnDefinition = "TEXT")
    private String excerptFi;
    @Column(length = 2000, columnDefinition = "TEXT")
    private String excerptEn;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private String author;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime publishDate;

    @Column(nullable = false)
    private boolean published;

    @Transient
    private String localizedTitle;

    @Transient
    private String localizedExcerpt;

    @Transient
    private String localizedContent;
}