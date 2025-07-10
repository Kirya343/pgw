package org.kirya343.main.controller.secure;

import org.kirya343.main.model.User;
import org.kirya343.main.model.listingModels.Location;
import org.kirya343.main.repository.LocationRepository;
import org.kirya343.main.model.Listing;
import org.kirya343.main.services.*;
import org.kirya343.main.services.components.AuthService;
import org.kirya343.main.services.components.StatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;
    private final ListingService listingService;
    private final StatService statService;
    private final StorageService storageService;
    private final AuthService authService;
    private final LocationRepository locationRepository;

    @GetMapping("/secure/account")
    public String getAccountPage(Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        // Получаем или создаем пользователя
        User user = userService.findUserFromOAuth2(oauth2User);

        List<Listing> listings = listingService.localizeAccountListings(user, locale);

        Map<String, Object> userStats = statService.getUserStats(user, locale);

        authService.validateAndAddAuthentication(model, oauth2User);

        // Передаем данные в модель
        model.addAttribute("listings", listings);
        model.addAttribute("stats", userStats);

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "account");

        return "secure/account";
    }

    @GetMapping("/secure/account/edit")
    public String editProfile(@AuthenticationPrincipal OAuth2User oauth2User, Model model) {

        authService.validateAndAddAuthentication(model, oauth2User);
        List<Location> countries = locationRepository.findByCityFalse();
        System.out.println("Найдено стран: " + countries.size());

        // Переменная для отображения активной страницы
        model.addAttribute("countries", countries);
        model.addAttribute("activePage", "edit");

        return "secure/account/edit";
    }
    @PostMapping("/secure/account/update")
    public String updateProfile(
            @ModelAttribute User updatedUser,
            @RequestParam(value = "image", required = false) MultipartFile avatarFile,
            @RequestParam("avatarType") String avatarType,
            @RequestParam(value = "phoneVisible", defaultValue = "false") boolean phoneVisible,
            @RequestParam(value = "emailVisible", defaultValue = "false") boolean emailVisible,
            @RequestParam List<String> languages,
            @RequestParam Long locationId,
            @AuthenticationPrincipal OAuth2User oAuth2User,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = userService.findUserFromOAuth2(oAuth2User);

            // Обновляем основные данные
            // currentUser.getLanguages().clear();
            currentUser.setLanguages(languages);

            currentUser.setLocation(locationRepository.findById(locationId).orElse(null));

            currentUser.setName(updatedUser.getName() != null ? updatedUser.getName() : currentUser.getName());
            currentUser.setPhone(updatedUser.getPhone() != null ? updatedUser.getPhone() : currentUser.getPhone());
            currentUser.setBio(updatedUser.getBio() != null ? updatedUser.getBio() : currentUser.getBio());
            currentUser.setAvatarType(avatarType); // Сохраняем тип аватара

            // Обновляем настройки конфиденциальности
            currentUser.setPhoneVisible(phoneVisible); // Устанавливаем настройку отображения телефона
            currentUser.setEmailVisible(emailVisible); // Устанавливаем настройку отображения email

            System.out.println("avatarFile: " + avatarFile);
            // Сохраняем новую аватарку, если загружена
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String filePath = storageService.storeAvatar(avatarFile, currentUser.getId());
                System.out.println("Avatar file path: " + filePath);
                currentUser.setAvatarUrl(filePath);
            }

            if ("default".equals(avatarType)) {
                currentUser.setAvatarUrl("/images/avatar-placeholder.png");
            }

            
            if ("google".equals(avatarType)) {
                currentUser.setAvatarUrl(oAuth2User.getAttribute("picture"));
            }

            // Сохраняем пользователя
            userService.save(currentUser);

            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении профиля");
            e.printStackTrace();
        }

        return "redirect:/secure/account/edit";
    }
}
