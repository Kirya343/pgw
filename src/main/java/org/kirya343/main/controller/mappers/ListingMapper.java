package org.kirya343.main.controller.mappers;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.chat.ListingDTO;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ListingMapper {

    public ListingDTO convertToDTO(Listing listing) {
        if (listing == null) {
            return null;
        }

        Locale locale = LocaleContextHolder.getLocale();

        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId());
        dto.setTitleRu(listing.getTitleRu());
        dto.setTitleEn(listing.getTitleEn());
        dto.setTitleFi(listing.getTitleFi());
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

        String title = null;
        String description = null;

        if ("fi".equals(locale.getLanguage()) && listing.getCommunityFi()) {
            title = listing.getTitleFi();
            description = listing.getDescriptionFi();
        } else if ("ru".equals(locale.getLanguage()) && listing.getCommunityRu()) {
            title = listing.getTitleRu();
            description = listing.getDescriptionRu();
        } else if ("en".equals(locale.getLanguage()) && listing.getCommunityEn()) {
            title = listing.getTitleEn();
            description = listing.getDescriptionEn();
        }

        // Резервный выбор
        if (title == null || description == null) {
            if (title == null) {
                title = firstNonNull(listing.getTitleFi(), listing.getTitleRu(), listing.getTitleEn());
            }
            if (description == null) {
                description = firstNonNull(listing.getDescriptionFi(), listing.getDescriptionRu(), listing.getDescriptionEn());
            }
        }

        dto.setLocalizedTitle(title);
        dto.setLocalizedDescription(description);

        return dto;
    }

    private String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
