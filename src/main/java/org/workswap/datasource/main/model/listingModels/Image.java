package org.workswap.datasource.main.model.listingModels;

import org.workswap.datasource.main.model.Listing;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path;

    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;
}