package org.kirya343.main.controller.mappers;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.DTOs.ListingDTO;
import org.kirya343.main.model.listingModels.ListingTranslation;
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
        String lang = locale.getLanguage();

        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId());
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

        // Получаем перевод из Map по ключу языка
        ListingTranslation translation = listing.getTranslations().get(lang);

        // Фоллбек — если нет перевода на текущий язык, возьмём первый доступный
        if (translation == null && !listing.getTranslations().isEmpty()) {
            translation = listing.getTranslations().values().iterator().next();
        }

        if (translation != null) {
            dto.setLocalizedTitle(translation.getTitle());
            dto.setLocalizedDescription(translation.getDescription());
        } else {
            dto.setLocalizedTitle(null);
            dto.setLocalizedDescription(null);
        }

        return dto;
    }
}