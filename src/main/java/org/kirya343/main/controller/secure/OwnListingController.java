package org.kirya343.main.controller.secure;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Location;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.LocationRepository;
import org.kirya343.main.services.*;
import org.kirya343.main.services.components.AdminCheckService;
import org.kirya343.main.services.components.AvatarService;
import org.kirya343.main.services.components.StatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/secure/listing")
public class OwnListingController {


    private final AvatarService avatarService;
    private final ListingService listingService;
    private final UserService userService;
    private final StorageService storageService;
    private final LocationRepository locationRepository;
    private final StatService statService;
    private final AdminCheckService adminCheckService;

    public OwnListingController(AvatarService avatarService, ListingService listingService, UserService userService, StorageService storageService, LocationRepository locationRepository, StatService statService, AdminCheckService adminCheckService) {
        this.avatarService = avatarService;
        this.listingService = listingService;
        this.userService = userService;
        this.storageService = storageService;
        this.locationRepository = locationRepository;
        this.statService = statService;
        this.adminCheckService = adminCheckService;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {

        boolean isAdmin = adminCheckService.isAdmin(oauth2User);
        model.addAttribute("isAdmin", isAdmin);

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

        double averageRating = statService.getAverageRating(user);
        model.addAttribute("rating", averageRating);

        model.addAttribute("user", user);

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
            @RequestParam(value = "titleRu", required = false) String titleRu,
            @RequestParam(value = "descriptionRu", required = false) String descriptionRu,
            @RequestParam(value = "titleFi", required = false) String titleFi,
            @RequestParam(value = "descriptionFi", required = false) String descriptionFi,
            @RequestParam(value = "titleEn", required = false) String titleEn,
            @RequestParam(value = "descriptionEn", required = false) String descriptionEn,
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

            // Устанавливаем описание и название для каждого языка
            listing.setTitleRu(titleRu);
            listing.setDescriptionRu(descriptionRu);
            listing.setTitleFi(titleFi);
            listing.setDescriptionFi(descriptionFi);
            listing.setTitleEn(titleEn);
            listing.setDescriptionEn(descriptionEn);

            // Здесь не нужно ничего отдельно устанавливать для communityRu/Fi/En — Spring сам их поставит!

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

            boolean isAdmin = adminCheckService.isAdmin(oauth2User);
            model.addAttribute("isAdmin", isAdmin);

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


            model.addAttribute("user", currentUser);
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
            @RequestParam(value = "communityRu", defaultValue = "false") boolean communityRu,
            @RequestParam(value = "communityFi", defaultValue = "false") boolean communityFi,
            @RequestParam(value = "communityEn", defaultValue = "false") boolean communityEn,
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
            existingListing.setTitleRu(listingData.getTitleRu());
            existingListing.setTitleEn(listingData.getTitleEn());
            existingListing.setTitleFi(listingData.getTitleFi());
            existingListing.setDescriptionRu(listingData.getDescriptionRu());
            existingListing.setDescriptionEn(listingData.getDescriptionEn());
            existingListing.setDescriptionFi(listingData.getDescriptionFi());
            existingListing.setPrice(listingData.getPrice());
            existingListing.setPriceType(listingData.getPriceType());
            existingListing.setCategory(listingData.getCategory());
            existingListing.setLocation(listingData.getLocation());
            existingListing.setActive(active);

            existingListing.setCommunityRu(communityRu);
            existingListing.setCommunityFi(communityFi);
            existingListing.setCommunityEn(communityEn);

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

