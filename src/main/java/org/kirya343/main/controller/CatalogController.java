package org.kirya343.main.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Resume;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.ResumeService;
import org.kirya343.main.services.components.AuthService;
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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ListingService listingService;
    private final ResumeService resumeService;
    private final AuthService authService;
    private final MessageSource messageSource;

    @GetMapping
    public String showCatalog(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "false") boolean hasReviews,
            Model model,
            Locale locale,
            HttpServletRequest request,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        Sort sort = switch (sortBy) {
            case "price" -> Sort.by("price");
            case "rating" -> Sort.by("rating").descending();
            case "popularity" -> Sort.by("views").descending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, 12, sort);

        // Получаем объявления по категории и языку
        Page<Listing> listingsPage;
        
        listingsPage = listingService.getListingsSorted(category, sortBy, pageable, searchQuery, hasReviews, locale);

        // Интегрируем логику локализации описания и названия
        listingService.localizeCatalogListings(listingsPage.getContent(), locale);

        List<Resume> resumes = new ArrayList<>();

        if ("find-help".equals(category)) {
            // Получаем опубликованные резюме
            resumes = resumeService.findPublishedResumes(pageable);
        }

        model.addAttribute("resumes", resumes);

        List<Listing> filteredListings = listingsPage.getContent().stream()
                //.filter(listing -> !available || listing.isAvailable())
                .filter(listing -> !hasReviews || (listing.getReviews() != null && !listing.getReviews().isEmpty()))
                .toList();

        // Добавляем данные в модель
        model.addAttribute("listings", filteredListings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", listingsPage.getTotalPages());
        model.addAttribute("category", category);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("hasReviews", hasReviews);

        // Переменная для отображения активной страницы
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("activePage", "catalog");

        authService.validateAndAddAuthentication(model, oauth2User);

        List<CategoryTab> categories = List.of(
            new CategoryTab("services", messageSource.getMessage("category.service", null, locale), 
                "services".equals(category)),  // Безопасное сравнение
            new CategoryTab("offer-service", messageSource.getMessage("category.offer-service", null, locale), 
                "offer-service".equals(category)),  // Безопасное сравнение
            new CategoryTab("product", messageSource.getMessage("category.product", null, locale), 
                "product".equals(category)),  // Безопасное сравнение
            new CategoryTab("find-help", messageSource.getMessage("category.helper", null, locale), 
                "find-help".equals(category))  // Безопасное сравнение
        );

        model.addAttribute("categories", categories);

        return "catalog";
    }

    @GetMapping("/sort")
    public String sortCatalogAjax(
            @RequestParam String category,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "false") boolean hasReviews,
            Model model,
            HttpServletRequest request,
            Locale locale
    ) {

        System.out.println("=== [AJAX SORTING] ===");
        System.out.println("Category: " + category);
        System.out.println("SortBy: " + sortBy);
        System.out.println("Page: " + page);
        System.out.println("Locale: " + locale);
        System.out.println("SearchQuery: " + searchQuery);

        Pageable pageable = PageRequest.of(page, 12);

        Page<Listing> listingsPage = listingService.getListingsSorted(category, sortBy, pageable, searchQuery, hasReviews, locale);
        System.out.println("Found listings: " + listingsPage.getTotalElements());

        listingService.localizeCatalogListings(listingsPage.getContent(), locale);

        model.addAttribute("listings", listingsPage);

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        System.out.println("Is AJAX request: " + isAjax);

        if (isAjax) {
            System.out.println("Returning fragment: ~{fragments/public/catalog-fragments :: content}");
            return "fragments/public/catalog-fragments :: content"; // HTML-фрагмент, который ты вставляешь в <div id="catalog-content">
        }

        return "catalog"; // fallback для обычной загрузки
    }   

    // Вспомогательный класс для представления вкладок категорий
    private record CategoryTab(String id, String title, boolean active) {}
}
