package org.kirya343.main.controller.secure;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.kirya343.main.model.DTOs.ListingForm;
import org.kirya343.main.model.DTOs.ListingTranslationDTO;
import org.kirya343.main.model.ModelsSettings.SearchParamType;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.kirya343.main.model.listingModels.Category;
import org.kirya343.main.model.listingModels.Image;
import org.kirya343.main.model.listingModels.ListingTranslation;
import org.kirya343.main.model.listingModels.Location;
import org.kirya343.main.repository.ImageRepository;
import org.kirya343.main.repository.LocationRepository;
import org.kirya343.main.services.CategoryService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.StorageService;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

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
    private final CategoryService categoryService;

    @GetMapping("/create")
    public String showCreateForm(Model model, 
                            @AuthenticationPrincipal OAuth2User oauth2User, 
                            Locale locale) {
        
        // 1. Получаем только корневые категории (без родителя)
        List<Category> rootCategories = categoryService.getRootCategories();
        model.addAttribute("rootCategories", rootCategories);
        
        List<Location> countries = locationRepository.findByCityFalse();
        model.addAttribute("countries", countries);
        
        // 3. Добавляем пустую форму
        model.addAttribute("listingForm", new ListingForm());
        
        // 4. Аутентификация (оставляем как было)
        authService.validateAndAddAuthentication(model, oauth2User);
        
        return "secure/listing/create";
    }

    @PostMapping("/create")
    public String createListing(
            @RequestParam(value = "uploadedImages", required = false) MultipartFile[] uploadedImages,
            @RequestParam(value = "imagePath", required = false) String imagePathParam,
            @ModelAttribute ListingForm form,
            @ModelAttribute Listing listing,
            @RequestParam Long categoryId,
            @RequestParam Long locationId,
            @AuthenticationPrincipal OAuth2User oauth2User,
            RedirectAttributes redirectAttributes
    ) {
        if (listing.getCategory() != null && !listing.getCategory().isLeaf()) {
            redirectAttributes.addFlashAttribute("error", "Можно выбрать только конечную категорию");
            return "redirect:/secure/listing/create";
        }
        try {
            String email = oauth2User.getAttribute("email");
            User currentUser = userService.findUser(email, SearchParamType.EMAIL);

            listing.setLocation(locationRepository.findById(locationId).orElse(null));
            listing.setAuthor(currentUser);
            listing.setCreatedAt(LocalDateTime.now());
            listing.setViews(0);
            listing.setRating(0.0);
            listing.setCategory(categoryService.getCategoryById(categoryId));

            // Получаем новые переводы из формы
            Map<String, ListingTranslationDTO> translationDTOs = form.getTranslations();

            Map<String, ListingTranslation> listingTranslations = new HashMap<>();
            listing.getCommunities().clear();

            for (Map.Entry<String, ListingTranslationDTO> entry : translationDTOs.entrySet()) {
                String lang = entry.getKey();
                ListingTranslationDTO dto = entry.getValue();

                System.out.println("LANG: " + lang + ", DTO: " + dto);

                ListingTranslation translation = new ListingTranslation();
                translation.setLanguage(lang);
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

                        if (Objects.equals(image.getOriginalFilename(), imagePathParam)) {
                            savedListing.setImagePath(imagePath);
                        }
                    }
                }
            }

            // Обновление основного изображения (если выбрано существующее)
            if (imagePathParam != null && !imagePathParam.isEmpty() &&
                    (uploadedImages == null || Arrays.stream(uploadedImages)
                        .map(MultipartFile::getOriginalFilename)
                        .filter(Objects::nonNull)
                        .noneMatch(name -> name.equals(imagePathParam)))) {
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
            RedirectAttributes redirectAttributes,
            Locale locale
    ) {
        try {

            List<Category> rootCategories = categoryService.getRootCategories();
            model.addAttribute("rootCategories", rootCategories);
            
            // Проверка, что текущий пользователь - автор объявления
            User user = userService.findUserFromOAuth2(oauth2User);
            Listing listing = listingService.getListingById(id);

            if (!listing.getAuthor().equals(user)) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете редактировать это объявление");
                return "redirect:/secure/account";
            }

            Long categoryId = listing.getCategory().getId();

            List<Location> countries = locationRepository.findByCityFalse();

            model.addAttribute("listing", listing);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("countries", countries);

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
            @ModelAttribute ListingForm form,
            @ModelAttribute Listing listing,
            @RequestParam(value = "uploadedImages", required = false) MultipartFile[] uploadedImages,
            @RequestParam(value = "deletedImages", required = false) String deletedImages,
            @RequestParam(value = "imagePath", required = false) String imagePathParam,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "testMode", defaultValue = "false") boolean testMode,
            @RequestParam Long locationId,
            @RequestParam Long categoryId,
            @AuthenticationPrincipal OAuth2User oauth2User,
            RedirectAttributes redirectAttributes
    ) {
        try {

            if (uploadedImages == null) {
                uploadedImages = new MultipartFile[0];
            }

            Listing existingListing = listingService.getListingById(id);
            User user = userService.findUserFromOAuth2(oauth2User);

            if (!existingListing.getAuthor().equals(user)) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете редактировать это объявление");
                return "redirect:/secure/account";
            }

            // Получаем новые переводы из формы
            Map<String, ListingTranslationDTO> translationDTOs = form.getTranslations();

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
            for (Map.Entry<String, ListingTranslationDTO> entry : translationDTOs.entrySet()) {
                String lang = entry.getKey();
                ListingTranslationDTO dto = entry.getValue();

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
            existingListing.setPrice(listing.getPrice());
            existingListing.setPriceType(listing.getPriceType());
            existingListing.setCategory(categoryService.getCategoryById(categoryId));
            existingListing.setLocation(locationRepository.findById(locationId).orElse(null));
            existingListing.setActive(active);
            existingListing.setTestMode(testMode);

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
                    System.out.println("Received file: " + image.getOriginalFilename() + ", size=" + image.getSize());
                    if (!image.isEmpty()) {
                        String imagePath = storageService.storeListingImage(image, savedListing.getId());
                        System.out.println("Stored image path: " + imagePath);

                        Image imageEntity = new Image();
                        imageEntity.setListing(savedListing);
                        imageEntity.setPath(imagePath);
                        imageRepository.save(imageEntity);

                        // Логика с основным изображением — проверь внимательно
                        if (Objects.equals(image.getOriginalFilename(), imagePathParam)) {
                            savedListing.setImagePath(imagePath);
                        }
                    }
                }
            }

            // Обновление основного изображения (если выбрано существующее)
            if (imagePathParam != null && !imagePathParam.isEmpty() &&
                    (uploadedImages == null || Arrays.stream(uploadedImages)
                        .map(MultipartFile::getOriginalFilename)
                        .filter(Objects::nonNull)
                        .noneMatch(name -> name.equals(imagePathParam)))) {
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

