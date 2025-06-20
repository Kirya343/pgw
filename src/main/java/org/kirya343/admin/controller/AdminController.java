package org.kirya343.admin.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.StatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StatService statService;
    private final ListingService listingService;
    private final UserService userService;

    @GetMapping
    public String index(Model model) {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Locale locale = Locale.of("ru");
        
        // Получаем статистику сайта
        Map<String, Object> stats = statService.getSiteStats(locale);
        
        // Получаем последние объявления и пользователей
        List<Listing> recentListings = listingService.getRecentListings(3);
        List<User> recentUsers = userService.getRecentUsers(3); 

        for (Listing listing : recentListings) {
            listingService.localizeListing(listing, locale);
        }

        // Добавляем данные в модель
        model.addAttribute("stats", stats);
        model.addAttribute("recentListings", recentListings);
        model.addAttribute("recentUsers", recentUsers);

        model.addAttribute("activePage", "dashboard");

        return "admin/dashboard";
    }

}
