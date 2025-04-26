package org.kirya343.main.controller;

import lombok.RequiredArgsConstructor;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ListingService listingService;
    private final UserService userService;
    private final AvatarService avatarService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String showCatalog(
            @RequestParam(defaultValue = "services") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(required = false) String location,
            Model model,
            Locale locale,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        // Определяем параметры сортировки
        Sort sort = switch (sortBy) {
            case "price" -> Sort.by("price");
            case "rating" -> Sort.by("rating").descending();
            case "popularity" -> Sort.by("views").descending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, 12, sort);

        // Получаем объявления по категории и языку
        Page<Listing> listingsPage;
        switch (category) {
            case "offer-service":
                listingsPage = findListingsByCategoryAndCommunity("offer-service", locale, pageable);
                break;
            case "find-help":
                listingsPage = findListingsByCategoryAndCommunity("find-help", locale, pageable);
                break;
            case "product":
                listingsPage = findListingsByCategoryAndCommunity("product", locale, pageable);
                break;
            default:
                listingsPage = findListingsByCategoryAndCommunity("services", locale, pageable);
        }

        // Интегрируем логику локализации описания и названия
        for (Listing listing : listingsPage.getContent()) {
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

            // Если нет значения для выбранного языка, пробуем другие языки
            if (title == null || description == null) {
                if (title == null) {
                    title = listing.getTitleFi() != null ? listing.getTitleFi() :
                            listing.getTitleRu() != null ? listing.getTitleRu() :
                                    listing.getTitleEn();
                }
                if (description == null) {
                    description = listing.getDescriptionFi() != null ? listing.getDescriptionFi() :
                            listing.getDescriptionRu() != null ? listing.getDescriptionRu() :
                                    listing.getDescriptionEn();
                }
            }

            // Сохраняем в транзиентные поля
            listing.setLocalizedTitle(title);
            listing.setLocalizedDescription(description);
        }

        // Добавляем данные в модель
        model.addAttribute("listings", listingsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", listingsPage.getTotalPages());
        model.addAttribute("category", category);
        model.addAttribute("sortBy", sortBy);

        // Добавляем информацию о пользователе, если авторизован
        if (oauth2User != null) {
            User user = userService.findUserFromOAuth2(oauth2User);
            String name = user.getName() != null ? user.getName() : oauth2User.getAttribute("name");
            String avatarPath = avatarService.resolveAvatarPath(user);

            model.addAttribute("isAuthenticated", true);
            model.addAttribute("userName", name != null ? name : "Пользователь");
            model.addAttribute("avatarUrl", avatarPath);
        } else {
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("userName", "Пользователь");
            model.addAttribute("avatarUrl", "/images/avatar-placeholder.jpg");
        }

        List<CategoryTab> categories = List.of(
                new CategoryTab("services", messageSource.getMessage("category.service", null, locale), category.equals("services")),
                new CategoryTab("offer-service", messageSource.getMessage("category.offer-service", null, locale), category.equals("offer-service")),
                new CategoryTab("product", messageSource.getMessage("category.product", null, locale), category.equals("product")),
                new CategoryTab("find-help", messageSource.getMessage("category.helper", null, locale), category.equals("find-help"))
        );

        model.addAttribute("categories", categories);

        return "catalog";
    }

    private Page<Listing> findListingsByCategoryAndCommunity(String category, Locale locale, Pageable pageable) {
        // В зависимости от языка выбираем нужное комьюнити
        if ("fi".equals(locale.getLanguage())) {
            return listingService.findActiveByCategoryAndCommunity("fi", category, pageable);
        } else if ("ru".equals(locale.getLanguage())) {
            return listingService.findActiveByCategoryAndCommunity("ru", category, pageable);
        } else if ("en".equals(locale.getLanguage())) {
            return listingService.findActiveByCategoryAndCommunity("en", category, pageable);
        } else {
            return listingService.findActiveByCategory(category, pageable); // для других языков
        }
    }

    // Вспомогательный класс для представления вкладок категорий
    private record CategoryTab(String id, String title, boolean active) {}
}
