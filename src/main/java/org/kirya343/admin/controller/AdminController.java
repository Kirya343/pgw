package org.kirya343.admin.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.StatService;
import org.kirya343.main.services.ResumeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/admin")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final StatService statService;
    private final ListingService listingService;
    private final UserService userService;

    public AdminController(StatService statService, ListingService listingService, UserService userService, ResumeService resumeService) {
        this.statService = statService;
        this.listingService = listingService;
        this.userService = userService;
    }

    @GetMapping
    public String index(Model model, Locale locale) {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Locale locale) {
        // Получаем статистику сайта
        Map<String, Object> stats = statService.getSiteStats(locale);

        // Получаем последние объявления и пользователей
        List<Listing> recentListings = listingService.getRecentListings(3);
        List<User> recentUsers = userService.getRecentUsers(3); 

        // Проставляем локализованные поля по схеме: ru -> fi -> en
        for (Listing listing : recentListings) {
            String title = null;
            String description = null;

            if (listing.getTitleRu() != null && !listing.getTitleRu().isBlank()) {
                title = listing.getTitleRu();
            } else if (listing.getTitleFi() != null && !listing.getTitleFi().isBlank()) {
                title = listing.getTitleFi();
            } else if (listing.getTitleEn() != null && !listing.getTitleEn().isBlank()) {
                title = listing.getTitleEn();
            }

            if (listing.getDescriptionRu() != null && !listing.getDescriptionRu().isBlank()) {
                description = listing.getDescriptionRu();
            } else if (listing.getDescriptionFi() != null && !listing.getDescriptionFi().isBlank()) {
                description = listing.getDescriptionFi();
            } else if (listing.getDescriptionEn() != null && !listing.getDescriptionEn().isBlank()) {
                description = listing.getDescriptionEn();
            }

            // Сохраняем в транзиентные поля
            listing.setLocalizedTitle(title);
            listing.setLocalizedDescription(description);
        }

        // Добавляем данные в модель
        model.addAttribute("stats", stats);
        model.addAttribute("recentListings", recentListings);
        model.addAttribute("recentUsers", recentUsers);

        model.addAttribute("activePage", "dashboard");

        return "admin/dashboard";
    }



    @GetMapping("/listings")
    public String listings(Model model) {
        List<Listing> allListings = listingService.getAllListings();
        model.addAttribute("listings", allListings);
        return "admin/listings";
    }

//    @GetMapping("/resumes")
//    public String resumes(Model model) {
//        List<Resume> allResumes = resumeService.getAllResumes();
//        model.addAttribute("resumes", allResumes);
//        return "admin/resumes";
//    }
//
//    @GetMapping("/users")
//    public String users(Model model) {
//        List<User> allUsers = adminService.getAllUsers();
//        model.addAttribute("users", allUsers);
//        return "admin/users";
//    }

    @GetMapping("/reviews")
    public String reviews() {
        return "admin/reviews";
    }

    @GetMapping("/questions")
    public String questions() {
        return "admin/questions";
    }

    @GetMapping("/settings")
    public String settings() {
        return "admin/settings";
    }

    @GetMapping("/localization")
    public String localization() {
        return "admin/localization";
    }
}
