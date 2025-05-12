package org.kirya343.main.controller.secure;

import org.kirya343.main.model.User;
import org.kirya343.main.model.Listing;
import org.kirya343.main.services.*;
import org.kirya343.main.services.components.AdminCheckService;
import org.kirya343.main.services.components.AvatarService;
import org.kirya343.main.services.components.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
public class AccountController {

    private final UserService userService;
    private final AvatarService avatarService;
    private final ListingService listingService;
    private final StatService statService;
    private final StorageService storageService;
    private final AdminCheckService adminCheckService;


    @Autowired
    public AccountController(UserService userService,
                             AvatarService avatarService, ListingService listingService, StatService statService, StorageService storageService, AdminCheckService adminCheckService) {
        this.userService = userService;
        this.avatarService = avatarService;
        this.listingService = listingService;
        this.statService = statService;
        this.storageService = storageService;
        this.adminCheckService = adminCheckService;
    }

    @GetMapping("/secure/account")
    public String getAccountPage(Model model, @AuthenticationPrincipal OAuth2User oauth2User, Locale locale) {
        if (oauth2User == null) {
            return "redirect:/login";
        }

        boolean isAdmin = adminCheckService.isAdmin(oauth2User);
        model.addAttribute("isAdmin", isAdmin);

        // Получаем или создаем пользователя
        User user = userService.findUserFromOAuth2(oauth2User);

        // Определяем URL аватара с приоритетом avatarUrl над picture
        String avatarPath = avatarService.resolveAvatarPath(user);

        // Получаем email и имя (используем данные из БД, а не напрямую из OAuth2)
        String email = user.getEmail() != null ? user.getEmail() : oauth2User.getAttribute("email");
        String name = user.getName();

        // Получаем список объявлений пользователя
        List<Listing> listings = listingService.getListingsByUser(user);

        // Обрабатываем каждый listing, чтобы задать title и description по нужному порядку языков
        for (Listing listing : listings) {
            String title = null;
            String description = null;

            if ("fi".equals(locale.getLanguage()) && listing.getCommunityFi()) {
                title = listing.getTitleFi();
                description = listing.getDescriptionFi();
            } else if ("ru".equals(locale.getLanguage()) && listing.getCommunityRu()) {
                title = listing.getTitleRu();
                description = listing.getDescriptionRu();
            } else if ("en".equals(locale.getLanguage()) && listing.getCommunityEn()) {
                title = listing.getTitleEn();
                description = listing.getDescriptionEn();
            }

            // Если нет значения для выбранного языка, пробуем другие языки
            if (title == null || description == null) {
                if (title == null) {
                    title = listing.getTitleFi() != null ? listing.getTitleFi() :
                            listing.getTitleRu() != null ? listing.getTitleRu() :
                                    listing.getTitleEn();
                }
                if (description == null) {
                    description = listing.getDescriptionFi() != null ? listing.getDescriptionFi() :
                            listing.getDescriptionRu() != null ? listing.getDescriptionRu() :
                                    listing.getDescriptionEn();
                }
            }

            // Сохраняем в транзиентные поля
            listing.setLocalizedTitle(title);
            listing.setLocalizedDescription(description);
        }

        // Получаем статистику
        int views = statService.getTotalViews(user);
        int responses = statService.getTotalResponses(user);
        int deals = statService.getCompletedDeals(user);
        double averageRating = statService.getAverageRating(user);

        model.addAttribute("userEmail", email != null ? email : "Email не распознан");
        model.addAttribute("userName", name != null ? name : "Пользователь");
        model.addAttribute("avatarPath", avatarPath);
        model.addAttribute("user", user);

        // Передаем данные в модель
        model.addAttribute("listings", listings);
        model.addAttribute("views", views);
        model.addAttribute("responses", responses);
        model.addAttribute("deals", deals);
        model.addAttribute("rating", averageRating);

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "account");

        return "secure/account";
    }

    @GetMapping("/secure/account/edit")
    public String editProfile(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = userService.findByEmail(principal.getAttribute("email"));
        String avatarPath = avatarService.resolveAvatarPath(user); // Формируем путь так же, как в getAccountPage
        String avatarUrlPath = (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty())
                ? "/" + user.getAvatarUrl()
                : "/images/upload-foto.png";

        double averageRating = statService.getAverageRating(user);
        model.addAttribute("rating", averageRating);

        model.addAttribute("user", user);
        model.addAttribute("avatarUrlPath", avatarUrlPath);
        model.addAttribute("avatarPath", avatarPath);

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "edit");

        return "secure/account/edit";
    }
    @PostMapping("/secure/account/update")
    public String updateProfile(
            @ModelAttribute User updatedUser,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
            @RequestParam("avatarType") String avatarType,
            @RequestParam(value = "phoneVisible", defaultValue = "false") boolean phoneVisible,
            @RequestParam(value = "emailVisible", defaultValue = "false") boolean emailVisible,
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = userService.findByEmail(principal.getAttribute("email"));

            // Обновляем основные данные
            currentUser.setName(updatedUser.getName() != null ? updatedUser.getName() : currentUser.getName());
            currentUser.setPhone(updatedUser.getPhone() != null ? updatedUser.getPhone() : currentUser.getPhone());
            currentUser.setBio(updatedUser.getBio() != null ? updatedUser.getBio() : currentUser.getBio());
            currentUser.setAvatarType(avatarType); // Сохраняем тип аватара

            // Обновляем настройки конфиденциальности
            currentUser.setPhoneVisible(phoneVisible); // Устанавливаем настройку отображения телефона
            currentUser.setEmailVisible(emailVisible); // Устанавливаем настройку отображения email

            // Сохраняем новую аватарку, если загружена
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String fileName = storageService.storeImage(avatarFile);
                currentUser.setAvatarUrl(fileName);
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
