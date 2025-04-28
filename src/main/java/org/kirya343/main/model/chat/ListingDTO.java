package org.kirya343.main.model.chat;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.kirya343.main.model.Listing;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ListingDTO {
    private Long id;
    private String titleRu;
    private String titleEn;
    private String titleFi;
    private String descriptionRu;
    private String descriptionEn;
    private String descriptionFi;
    private String localizedTitle;
    private String localizedDescription;
    private double price;
    private String priceType;
    private String category;
    private String location;
    private double rating;
    private int views;
    private LocalDateTime createdAt;
    private boolean active;
    private boolean availableNow;
    private String imagePath;
}