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

    // Конструктор для преобразования Listing в ListingDTO
    public ListingDTO convertToListingDTO(Listing listing) {
        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId());  // Используем объект dto для присваивания значений
        dto.setTitleRu(listing.getTitleRu());
        dto.setTitleEn(listing.getTitleEn());
        dto.setTitleFi(listing.getTitleFi());  // Исправлено на titleFi
        dto.setDescriptionRu(listing.getDescriptionRu());
        dto.setDescriptionEn(listing.getDescriptionEn());
        dto.setDescriptionFi(listing.getDescriptionFi());
        dto.setLocalizedTitle(listing.getLocalizedTitle());
        dto.setLocalizedDescription(listing.getLocalizedDescription());
        dto.setPrice(listing.getPrice());
        dto.setPriceType(listing.getPriceType());
        dto.setCategory(listing.getCategory());
        dto.setLocation(listing.getLocation());
        dto.setRating(listing.getRating());
        dto.setViews(listing.getViews());
        dto.setCreatedAt(listing.getCreatedAt());
        dto.setActive(listing.isActive());
        dto.setAvailableNow(listing.isAvailableNow());
        dto.setImagePath(listing.getImagePath());
        return dto;
    }
}