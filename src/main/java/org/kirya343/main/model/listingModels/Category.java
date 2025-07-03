package org.kirya343.main.model.listingModels;

import java.util.ArrayList;
import java.util.List;

import org.kirya343.main.model.Listing;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private boolean leaf = false; // Является ли конечной категорией

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Listing> listings = new ArrayList<>();

    // Конструкторы
    public Category() {}

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }
}
