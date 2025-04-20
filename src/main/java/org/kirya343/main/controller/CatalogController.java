package org.kirya343.main.controller;

import lombok.RequiredArgsConstructor;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.UserService;
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

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ListingService listingService;
    private final UserService userService;
    private final AvatarService avatarService;

    @GetMapping
    public String showCatalog(
            @RequestParam(defaultValue = "services") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(required = false) String location,
            Model model,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        // Определяем параметры сортировки
        Sort sort = switch (sortBy) {
            case "price" -> Sort.by("price");
            case "rating" -> Sort.by("rating").descending();
            case "popularity" -> Sort.by("views").descending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, 12, sort);

        // Получаем объявления по категории
        Page<Listing> listingsPage = switch (category) {
            case "offer-service" -> listingService.findActiveByCategory("offer-service", pageable);
            case "find-help" -> listingService.findActiveByCategory("find-help", pageable);
            case "product" -> listingService.findActiveByCategory("product", pageable);
            default -> listingService.findActiveByCategory("services", pageable);
        };

        // Добавляем данные в модель
        model.addAttribute("listings", listingsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", listingsPage.getTotalPages());
        model.addAttribute("category", category);
        model.addAttribute("sortBy", sortBy);

        // Добавляем информацию о пользователе, если авторизован
        if (oauth2User != null) {
            User user = userService.findOrCreateUserFromOAuth2(oauth2User);
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

        model.addAttribute("categories", List.of(
                new CategoryTab("services", "Услуги", category.equals("services")),
                new CategoryTab("offer-service", "Нужна услуга", category.equals("offer-service")),
                new CategoryTab("product", "Товары", category.equals("product")),
                new CategoryTab("find-help", "Найти помощника", category.equals("find-help"))
        ));

        return "catalog";
    }

    // Вспомогательный класс для представления вкладок категорий
    private record CategoryTab(String id, String title, boolean active) {}
}
