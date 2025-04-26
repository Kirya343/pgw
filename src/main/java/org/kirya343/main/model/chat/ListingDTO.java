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

    // Конструктор для преобразования Listing в ListingDTO
    public ListingDTO convertToListingDTO(Listing listing) {
        ListingDTO dto = new ListingDTO();
        this.id = listing.getId();
        this.titleRu = listing.getTitleRu();
        this.titleEn = listing.getTitleEn();
        this.titleFi = listing.getTitleEn();
        this.descriptionRu = listing.getDescriptionRu();
        this.descriptionEn = listing.getDescriptionEn();
        this.descriptionFi = listing.getDescriptionFi();
        this.price = listing.getPrice();
        this.priceType = listing.getPriceType();
        this.category = listing.getCategory();
        this.location = listing.getLocation();
        this.rating = listing.getRating();
        this.views = listing.getViews();
        this.createdAt = listing.getCreatedAt();
        this.active = listing.isActive();
        this.availableNow = listing.isAvailableNow();
        this.imagePath = listing.getImagePath();
        return dto;
    } // Геттеры и сеттеры (сгенерированные Lombok)
}

