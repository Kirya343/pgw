package org.kirya343.main.model.listingModels;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String country; // Например: "Финляндия", "Россия"

    @Column(nullable = false, unique = true)
    private String city; // Например: "Куопио", "Санкт-Петербург"

    @Column(nullable = false, unique = true)
    private String name; // Например: "Финляндия, Копио", "Россия, Санкт-Петербург"
}
