package org.workswap.main.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.main.services.UserService;
import org.workswap.main.services.components.AuthService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUsersController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public String usersList(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        authService.validateAndAddAuthentication(model, oauth2User);
        List<User> usersList = userService.findAll();
        model.addAttribute("usersList", usersList);
        model.addAttribute("activePage", "admin-users");
        return "admin/users/users-list";
    }

    @GetMapping("/view/{id}")
    public String currentUser(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            authService.validateAndAddAuthentication(model, oauth2User);
            User user = userService.findUser(id.toString(), SearchParamType.ID);
            model.addAttribute("user", user);
            model.addAttribute("activePage", "admin-users");
            return "admin/users/view-user";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/update/{id}")
    public String modifyUser(@PathVariable Long id,
                           @ModelAttribute User user,
                           @AuthenticationPrincipal OAuth2User oauth2User,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении пользователя");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении пользователя");
        }
        return "redirect:/admin/users";
    }
}