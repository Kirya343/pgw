package org.kirya343.main.controller;

import java.util.List;
import java.util.Locale;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.listingModels.Category;
import org.kirya343.main.repository.CategoryRepository;
import org.kirya343.main.services.CategoryService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.components.AuthService;
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
    private final CategoryRepository categoryRepository;
    private final AuthService authService;
    private final CategoryService categoryService;

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

        /* Category categoryType = categoryRepository.findByName(category);

        Sort sort = switch (sortBy) {
            case "price" -> Sort.by("price");
            case "rating" -> Sort.by("rating").descending();
            case "popularity" -> Sort.by("views").descending();
            default -> Sort.by("createdAt").descending();
        }; */

        /* Page<Listing> listingsPage = listingService.getListingsSorted(categoryType, sortBy, pageable, searchQuery, hasReviews, locale);
        listingService.localizeCatalogListings(listingsPage.getContent(), locale); */

        /* List<Listing> filteredListings = listingsPage.getContent().stream()
                .filter(listing -> !hasReviews || (listing.getReviews() != null && !listing.getReviews().isEmpty()))
                .toList(); */

        // Добавляем данные в модель
        model.addAttribute("currentPage", page);

        model.addAttribute("hasReviews", hasReviews);
        model.addAttribute("category", category);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("searchQuery", searchQuery);
        
        List<Category> rootCategories = categoryService.getRootCategories();
        model.addAttribute("rootCategories", rootCategories);

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "catalog");

        authService.validateAndAddAuthentication(model, oauth2User);

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

        Category categoryType = categoryRepository.findByName(category);

        Page<Listing> listingsPage = listingService.getListingsSorted(categoryType, sortBy, pageable, searchQuery, hasReviews, locale);
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

}
