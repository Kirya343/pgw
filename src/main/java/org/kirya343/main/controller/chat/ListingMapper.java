package org.kirya343.main.controller.chat;


import org.kirya343.main.model.Listing;
import org.kirya343.main.model.chat.ListingDTO;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {

    public ListingDTO toDTO(Listing listing) {
        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
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
