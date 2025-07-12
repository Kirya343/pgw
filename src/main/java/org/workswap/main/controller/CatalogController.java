package org.workswap.main.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.listingModels.Category;
import org.workswap.datasource.main.model.listingModels.Location;
import org.workswap.datasource.main.repository.CategoryRepository;
import org.workswap.datasource.main.repository.LocationRepository;
import org.workswap.main.services.CategoryService;
import org.workswap.main.services.ListingService;
import org.workswap.main.services.UserService;
import org.workswap.main.services.components.AuthService;

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
    private final UserService userService;
    private final LocationRepository locationRepository;

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
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "false") boolean hasReviews,
            @RequestParam(required = false) String location,
            @AuthenticationPrincipal OAuth2User oauth2User,
            Model model,
            HttpServletRequest request,
            Locale locale
    ) {
        User user = null;
        List<String> languages = new ArrayList<>();
        Location locationType = null;
        if (oauth2User != null) {
            user = userService.findUserFromOAuth2(oauth2User);
            languages = user.getLanguages();
        }

        if (location == null && user != null) {
            locationType = user.getLocation();
        } else {
            locationType = locationRepository.findByName(location);
        }
        String lang = locale.getLanguage();

        if (!languages.contains(lang)) {
            languages.add(lang);
        }

        Pageable pageable = PageRequest.of(page, 12);

        Category categoryType = categoryRepository.findByName(category);

        Page<Listing> listingsPage = listingService.getListingsSorted(categoryType, sortBy, pageable, locationType, searchQuery, hasReviews, languages);

        System.out.println("Found listings: " + listingsPage.getTotalElements());

        listingService.localizeCatalogListings(listingsPage.getContent(), locale);

        model.addAttribute("listings", listingsPage);

        return "fragments/public/catalog-fragments :: content";
    }   

}
