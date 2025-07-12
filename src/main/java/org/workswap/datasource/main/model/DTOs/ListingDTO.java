package org.workswap.datasource.main.model.DTOs;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Long categoryId;
    private String location;
    private double rating;
    private int views;
    private LocalDateTime createdAt;
    private boolean active;
    private boolean availableNow;
    private String imagePath;
}