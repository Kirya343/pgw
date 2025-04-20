package org.kirya343.main.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Например: "Москва", "Санкт-Петербург"
}
