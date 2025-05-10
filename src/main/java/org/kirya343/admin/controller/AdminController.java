package org.kirya343.admin.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Resume;
import org.kirya343.main.model.User;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.ResumeService;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.StatService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final ResumeService resumeService;

    public AdminController(StatService statService, ListingService listingService, UserService userService, ResumeService resumeService) {
        this.statService = statService;
        this.listingService = listingService;
        this.userService = userService;
        this.resumeService = resumeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Locale locale) {
        // Получаем статистику сайта с учётом локали
        Map<String, Object> stats = statService.getSiteStats(locale);

        // Получаем последние объявления и пользователей
        List<Listing> recentListings = listingService.getRecentListings(3);
        List<User> recentUsers = userService.getRecentUsers(3);

        // Добавляем данные в модель
        model.addAttribute("stats", stats);
        model.addAttribute("recentListings", recentListings);
        model.addAttribute("recentUsers", recentUsers);

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

    @GetMapping("/news")
    public String news() {
        return "admin/news";
    }

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
