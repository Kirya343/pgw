package org.kirya343.main.controller.secure;

import org.kirya343.main.model.listingModels.Image;
import org.kirya343.main.model.listingModels.ListingTranslation;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Location;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.LocationRepository;
import org.kirya343.main.services.*;
import org.kirya343.main.services.components.AuthService;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.kirya343.main.repository.ImageRepository;

@Controller
@RequestMapping("/secure/listing")
@RequiredArgsConstructor
public class OwnListingController {

    private final ImageRepository imageRepository;
    private final ListingService listingService;
    private final UserService userService;
    private final StorageService storageService;
    private final LocationRepository locationRepository;
    private final AuthService authService;
    private final MessageSource messageSource;


    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {

        Map<String, String> categories = Map.of(
                "services", messageSource.getMessage("category.service", null, locale),
                "offer-service", messageSource.getMessage("category.offer-service", null, locale),
                "product", messageSource.getMessage("category.product", null, locale)
        );
        model.addAttribute("categories", categories);

        List<Location> locations = locationRepository.findAllByOrderByNameAsc();
        model.addAttribute("locations", locations);

        authService.validateAndAddAuthentication(model, oauth2User);

        model.addAttribute("listing", new Listing());
        return "secure/listing/create";
    }

    @PostMapping("/create")
    public String createListing(
            @ModelAttribute Listing listing,
            @RequestParam(value = "uploadedImages", required = false) MultipartFile[] uploadedImages,
            @RequestParam(value = "deletedImages", required = false) String deletedImages,
            @RequestParam(value = "imagePath", required = false) String imagePathParam,
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

            // Устанавливаем локализованные данные
            listing.setTitleRu(titleRu);
            listing.setDescriptionRu(descriptionRu);
            listing.setTitleFi(titleFi);
            listing.setDescriptionFi(descriptionFi);
            listing.setTitleEn(titleEn);
            listing.setDescriptionEn(descriptionEn);

            // Сначала сохраняем объявление, чтобы получить ID
            Listing savedListing = listingService.saveAndReturn(listing);

            // Удаление отмеченных изображений
            if (deletedImages != null && !deletedImages.isEmpty()) {
                Arrays.stream(deletedImages.split(","))
                    .map(Long::parseLong)
                    .forEach(imageId -> {
                        // Не удаляем, если это текущее основное изображение
                        if (savedListing.getImagePath() == null || 
                            !imageRepository.findById(imageId).get().getPath().equals(savedListing.getImagePath())) {
                            imageRepository.deleteById(imageId);
                        }
                    });
            }

            // Добавление новых изображений
            if (uploadedImages != null) {
                for (MultipartFile image : uploadedImages) {
                    if (!image.isEmpty()) {
                        String imagePath = storageService.storeListingImage(image, savedListing.getId());
                        Image imageEntity = new Image();
                        imageEntity.setListing(savedListing);
                        imageEntity.setPath(imagePath);
                        imageRepository.save(imageEntity);
                        
                        // Если это выбранное основное изображение
                        if (image.getOriginalFilename().equals(imagePathParam)) {
                            savedListing.setImagePath(imagePath);
                        }
                    }
                }
            }

            // Обновление основного изображения (если выбрано существующее)
            if (imagePathParam != null && !imagePathParam.isEmpty() && 
                (uploadedImages == null || !Arrays.stream(uploadedImages)
                    .anyMatch(file -> file.getOriginalFilename().equals(imagePathParam)))) {
                savedListing.setImagePath(imagePathParam);
            }

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
            User user = userService.findUserFromOAuth2(oauth2User);
            Listing listing = listingService.getListingById(id);

            if (!listing.getAuthor().equals(user)) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете редактировать это объявление");
                return "redirect:/secure/account";
            }

            Map<String, String> categories = Map.of(
                    "services", "Услуга",
                    "offer-service", "Запрос на услугу",
                    "product", "Товар"
            );

            List<Location> locations = locationRepository.findAllByOrderByNameAsc();

            model.addAttribute("listing", listing);
            model.addAttribute("categories", categories);
            model.addAttribute("locations", locations);

            authService.validateAndAddAuthentication(model, oauth2User);

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
            @RequestParam(value = "communities", required = false) List<String> communities,
            @RequestParam(value = "uploadedImages", required = false) MultipartFile[] uploadedImages,
            @RequestParam(value = "deletedImages", required = false) String deletedImages,
            @RequestParam(value = "imagePath", required = false) String imagePathParam,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @AuthenticationPrincipal OAuth2User oauth2User,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Listing existingListing = listingService.getListingById(id);
            User user = userService.findUserFromOAuth2(oauth2User);

            if (communities != null) {
                existingListing.setCommunities(new ArrayList<>(communities));
            } else {
                redirectAttributes.addFlashAttribute("error",
                    "Ошибка при обновлении объявления: не указаны целевые сообщества");
                return "redirect:/secure/listing/edit/" + id;
            }

            if (!existingListing.getAuthor().equals(user)) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете редактировать это объявление");
                return "redirect:/secure/account";
            }

            Map<String, ListingTranslation> translations = existingListing.getTranslations();
            if (translations == null) {
                translations = new HashMap<>();
                existingListing.setTranslations(translations);
            }

            for (String lang : existingListing.getCommunities()) {
                ListingTranslation translation = translations.get(lang);
                if (translation == null) {
                    translation = new ListingTranslation();
                    translation.setListing(existingListing);
                    translation.setLanguage(lang);
                    translations.put(lang, translation);
                }

                // Устанавливаем title и description по языку из listingData
                switch (lang) {
                    case "ru":
                        translation.setTitle(listingData.getTitleRu());
                        translation.setDescription(listingData.getDescriptionRu());
                        break;
                    case "fi":
                        translation.setTitle(listingData.getTitleFi());
                        translation.setDescription(listingData.getDescriptionFi());
                        break;
                    case "en":
                        translation.setTitle(listingData.getTitleEn());
                        translation.setDescription(listingData.getDescriptionEn());
                        break;
                    default:
                        // Если будут другие языки — нужно как-то получать значения из формы,
                        // либо игнорировать (или расширить listingData)
                        break;
                }
            }
            existingListing.setPrice(listingData.getPrice());
            existingListing.setPriceType(listingData.getPriceType());
            existingListing.setCategory(listingData.getCategory());
            existingListing.setLocation(listingData.getLocation());
            existingListing.setActive(active);

            // Удаление отмеченных изображений
            if (deletedImages != null && !deletedImages.isEmpty()) {
                Arrays.stream(deletedImages.split(","))
                    .map(Long::parseLong)
                    .forEach(imageId -> {
                        // Не удаляем, если это текущее основное изображение
                        if (existingListing.getImagePath() == null || 
                            !imageRepository.findById(imageId).get().getPath().equals(existingListing.getImagePath())) {
                            imageRepository.deleteById(imageId);
                        }
                    });
            }

            // Добавление новых изображений
            if (uploadedImages != null) {
                for (MultipartFile image : uploadedImages) {
                    if (!image.isEmpty()) {
                        String imagePath = storageService.storeListingImage(image, existingListing.getId());
                        Image imageEntity = new Image();
                        imageEntity.setListing(existingListing);
                        imageEntity.setPath(imagePath);
                        imageRepository.save(imageEntity);
                        
                        // Если это выбранное основное изображение
                        if (image.getOriginalFilename().equals(imagePathParam)) {
                            existingListing.setImagePath(imagePath);
                        }
                    }
                }
            }

            // Обновление основного изображения (если выбрано существующее)
            if (imagePathParam != null && !imagePathParam.isEmpty() && 
                (uploadedImages == null || !Arrays.stream(uploadedImages)
                    .anyMatch(file -> file.getOriginalFilename().equals(imagePathParam)))) {
                existingListing.setImagePath(imagePathParam);
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
            User user = userService.findUserFromOAuth2(oauth2User);

            if (!listing.getAuthor().equals(user)) {
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

