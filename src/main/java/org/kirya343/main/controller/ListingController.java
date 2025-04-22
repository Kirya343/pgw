package org.kirya343.main.controller;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Location;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.LocationRepository;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.StorageService;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/secure/listing")
public class ListingController {


    private final AvatarService avatarService;

    @Autowired
    private ListingService listingService;

    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private LocationRepository locationRepository;

    public ListingController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {

        Map<String, String> categories = Map.of(
                "services", "Услуга",
                "offer-service", "Запрос на услугу",
                "product", "Товар"
        );
        model.addAttribute("categories", categories);
        // Получаем email из OAuth2 аутентификации
        User user = userService.findUserFromOAuth2(oauth2User);
        String name = user.getName();
        String avatarPath = avatarService.resolveAvatarPath(user);

        List<Location> locations = locationRepository.findAllByOrderByNameAsc();
        model.addAttribute("locations", locations);

        // Передаём в шаблон
        model.addAttribute("userName", name != null ? name : "Пользователь");
        model.addAttribute("avatarPath", avatarPath);

        model.addAttribute("listing", new Listing());
        model.addAttribute("priceTypes", List.of("Фиксированная", "Договорная"));
        return "secure/listing/create";
    }

    @PostMapping("/create")
    public String createListing(
            @ModelAttribute Listing listing,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal OAuth2User oauth2User,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String email = oauth2User.getAttribute("email");
            User currentUser = userService.findByEmail(email);

            listing.setAuthor(currentUser);
            listing.setCreatedAt(LocalDateTime.now());
            listing.setViews(0);
            listing.setRating(0.0);

            if (!image.isEmpty()) {
                try {
                    String imagePath = storageService.storeImage(image);
                    listing.setImagePath(imagePath);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error",
                            "Ошибка загрузки изображения: " + e.getMessage());
                    return "redirect:/secure/listing/create";
                }
            }

            listingService.save(listing);
            redirectAttributes.addFlashAttribute("success", "Объявление успешно создано!");
            return "redirect:/secure/account";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при создании объявления: " + e.getMessage());
            return "redirect:/secure/listing/create";
        }
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal OAuth2User oauth2User,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Проверка, что текущий пользователь - автор объявления
            String email = oauth2User.getAttribute("email");
            User currentUser = userService.findByEmail(email);
            Listing listing = listingService.getListingById(id);

            if (!listing.getAuthor().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете редактировать это объявление");
                return "redirect:/secure/account";
            }

            Map<String, String> categories = Map.of(
                    "services", "Услуга",
                    "offer-service", "Запрос на услугу",
                    "product", "Товар"
            );

            List<Location> locations = locationRepository.findAllByOrderByNameAsc();

            String name = currentUser.getName();
            String avatarPath = avatarService.resolveAvatarPath(currentUser);

            model.addAttribute("listing", listing);
            model.addAttribute("categories", categories);
            model.addAttribute("locations", locations);
            model.addAttribute("userName", name != null ? name : "Пользователь");
            model.addAttribute("avatarPath", avatarPath);

            return "secure/listing/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при загрузке объявления: " + e.getMessage());
            return "redirect:/secure/account";
        }
    }

    @PostMapping("/update/{id}")
    public String updateListing(
            @PathVariable Long id,
            @ModelAttribute Listing listingData,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @AuthenticationPrincipal OAuth2User oauth2User,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Listing existingListing = listingService.getListingById(id);

            // Проверка авторства
            String email = oauth2User.getAttribute("email");
            User currentUser = userService.findByEmail(email);

            if (!existingListing.getAuthor().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете редактировать это объявление");
                return "redirect:/secure/account";
            }

            // Обновление полей
            existingListing.setTitle(listingData.getTitle());
            existingListing.setDescription(listingData.getDescription());
            existingListing.setPrice(listingData.getPrice());
            existingListing.setPriceType(listingData.getPriceType());
            existingListing.setCategory(listingData.getCategory());
            existingListing.setLocation(listingData.getLocation());
            existingListing.setActive(active);

            // Обновление изображения, если загружено новое
            if (image != null && !image.isEmpty()) {
                try {
                    String imagePath = storageService.storeImage(image);
                    existingListing.setImagePath(imagePath);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error",
                            "Ошибка загрузки изображения: " + e.getMessage());
                    return "redirect:/secure/listing/edit/" + id;
                }
            }

            listingService.save(existingListing);
            redirectAttributes.addFlashAttribute("success", "Объявление успешно обновлено!");
            return "redirect:/secure/account";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при обновлении объявления: " + e.getMessage());
            return "redirect:/secure/listing/edit/" + id;
        }
    }
    @PostMapping("/delete/{id}")
    public String deleteListing(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User oauth2User,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Listing listing = listingService.getListingById(id);

            // Проверка авторства
            String email = oauth2User.getAttribute("email");
            User currentUser = userService.findByEmail(email);

            if (!listing.getAuthor().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете удалить это объявление");
                return "redirect:/secure/account";
            }

            listingService.deleteListing(id);
            redirectAttributes.addFlashAttribute("success", "Объявление успешно удалено!");
            return "redirect:/secure/account";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при удалении объявления: " + e.getMessage());
            return "redirect:/secure/account";
        }
    }
}

