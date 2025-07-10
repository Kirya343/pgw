package org.kirya343.main.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.model.listingModels.Category;
import org.kirya343.main.repository.CategoryRepository;
import org.kirya343.main.services.CategoryService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.AuthService;
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
            @RequestParam String category,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false, defaultValue = "false") boolean hasReviews,
            @AuthenticationPrincipal OAuth2User oauth2User,
            Model model,
            HttpServletRequest request,
            Locale locale
    ) {
        User user = null;
        List<String> languages = new ArrayList<>();
        if (oauth2User != null) {
            user = userService.findUserFromOAuth2(oauth2User);
            languages = user.getLanguages();
        }
        String lang = locale.getLanguage();

        if (!languages.contains(lang)) {
            languages.add(lang);
        }

        Pageable pageable = PageRequest.of(page, 12);

        Category categoryType = categoryRepository.findByName(category);

        Page<Listing> listingsPage = listingService.getListingsSorted(categoryType, sortBy, pageable, searchQuery, hasReviews, languages);

        System.out.println("Found listings: " + listingsPage.getTotalElements());

        listingService.localizeCatalogListings(listingsPage.getContent(), locale);

        model.addAttribute("listings", listingsPage);

        return "fragments/public/catalog-fragments :: content";
    }   

}
