package org.kirya343.main.controller.secure;

import org.kirya343.main.model.listingModels.Image;
import org.kirya343.main.model.listingModels.ListingTranslation;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Location;
import org.kirya343.main.model.User;
import org.kirya343.main.model.DTOs.ListingForm;
import org.kirya343.main.model.DTOs.TranslationDTO;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
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
            @RequestParam(value = "imagePath", required = false) String imagePathParam,
            @ModelAttribute ListingForm form,
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

            Map<String, TranslationDTO> translationDTOs = form.getTranslations();
            Map<String, ListingTranslation> listingTranslations = new HashMap<>();
            listing.getCommunities().clear();

            for (Map.Entry<String, TranslationDTO> entry : translationDTOs.entrySet()) {
                String lang = entry.getKey();
                TranslationDTO dto = entry.getValue();

                ListingTranslation translation = new ListingTranslation();
                translation.setLanguage(lang);
                System.out.println("Creating translation for language: " + lang);
                System.out.println("Title: " + dto.getTitle());
                System.out.println("Description: " + dto.getDescription());
                translation.setTitle(dto.getTitle());
                translation.setDescription(dto.getDescription());
                translation.setListing(listing); // связь с родителем

                listingTranslations.put(lang, translation);
                listing.getCommunities().add(lang);
            }

            listing.setTranslations(listingTranslations);

            // Сохраняем Listing вместе с переводами
            Listing savedListing = listingService.saveAndReturn(listing);

            // Добавление новых изображений
            if (uploadedImages != null) {
                for (MultipartFile image : uploadedImages) {
                    if (!image.isEmpty()) {
                        String imagePath = storageService.storeListingImage(image, savedListing.getId());
                        Image imageEntity = new Image();
                        imageEntity.setListing(savedListing);
                        imageEntity.setPath(imagePath);
                        imageRepository.save(imageEntity);

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

            // После всех изменений повторно сохраним listing
            listingService.save(savedListing);

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

            // Сформировать Map<String, Map<String, String>> для JSON с переводами
            Map<String, Map<String, String>> translationsMap = new HashMap<>();
            listing.getTranslations().forEach((lang, translation) -> {
                Map<String, String> data = new HashMap<>();
                data.put("title", translation.getTitle());
                data.put("description", translation.getDescription());
                translationsMap.put(lang, data);
            });

            ObjectMapper mapper = new ObjectMapper();
            String translationsJson = mapper.writeValueAsString(translationsMap);

            model.addAttribute("translationsJson", translationsJson);

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
            @ModelAttribute ListingForm form,
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

            if (!existingListing.getAuthor().equals(user)) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете редактировать это объявление");
                return "redirect:/secure/account";
            }

            // Получаем новые переводы из формы
            Map<String, TranslationDTO> translationDTOs = form.getTranslations();

            // Чистим communities
            existingListing.getCommunities().clear();

            // Работаем с коллекцией переводов в existingListing
            Map<String, ListingTranslation> currentTranslations = existingListing.getTranslations();
            if (currentTranslations == null) {
                currentTranslations = new HashMap<>();
                existingListing.setTranslations(currentTranslations);
            }

            // Удаляем переводы, которых больше нет в новых данных
            currentTranslations.entrySet().removeIf(entry -> !translationDTOs.containsKey(entry.getKey()));

            // Обновляем или добавляем переводы
            for (Map.Entry<String, TranslationDTO> entry : translationDTOs.entrySet()) {
                String lang = entry.getKey();
                TranslationDTO dto = entry.getValue();

                if (currentTranslations.containsKey(lang)) {
                    ListingTranslation existingTranslation = currentTranslations.get(lang);
                    existingTranslation.setTitle(dto.getTitle());
                    existingTranslation.setDescription(dto.getDescription());
                } else {
                    ListingTranslation translation = new ListingTranslation();
                    translation.setLanguage(lang);
                    translation.setTitle(dto.getTitle());
                    translation.setDescription(dto.getDescription());
                    translation.setListing(existingListing);

                    currentTranslations.put(lang, translation);
                }

                // Добавляем в communities
                existingListing.getCommunities().add(lang);
            }

            // Обновляем остальные поля
            existingListing.setPrice(listingData.getPrice());
            existingListing.setPriceType(listingData.getPriceType());
            existingListing.setCategory(listingData.getCategory());
            existingListing.setLocation(listingData.getLocation());
            existingListing.setActive(active);

            // Сохраняем Listing вместе с переводами
            Listing savedListing = listingService.saveAndReturn(existingListing);

            if (deletedImages != null && !deletedImages.isEmpty()) {
                Arrays.stream(deletedImages.split(","))
                    .map(Long::parseLong)
                    .forEach(imageId -> {
                        imageRepository.findById(imageId).ifPresent(img -> {
                            // Не удаляем если это текущее основное изображение
                            if (savedListing.getImagePath() == null || 
                                !img.getPath().equals(savedListing.getImagePath())) {
                                imageRepository.deleteById(imageId);
                            }
                        });
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

            // После всех изменений повторно сохраним listing
            listingService.save(savedListing);
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

