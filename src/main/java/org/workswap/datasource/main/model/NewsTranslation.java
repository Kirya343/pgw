package org.workswap.datasource.main.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class NewsTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;

    private String title;
    @Column(length = 1000)
    private String shortDescription;
    @Column(length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "news_id")
    private News news;
}
