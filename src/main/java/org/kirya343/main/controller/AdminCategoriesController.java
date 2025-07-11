package org.kirya343.main.controller;

import org.kirya343.main.model.ModelsSettings.SearchParamType;
import org.kirya343.main.model.DTOs.CategoryDTO;
import org.kirya343.main.model.listingModels.Category;
import org.kirya343.main.repository.CategoryRepository;
import org.kirya343.main.services.CategoryService;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.AuthService;
import org.kirya343.main.services.components.RoleCheckService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoriesController {

    private final CategoryRepository categoryRepository;
    private final RoleCheckService roleCheckService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public String categoryList(Model model, Locale locale, @AuthenticationPrincipal OAuth2User oauth2User) {

        authService.validateAndAddAuthentication(model, oauth2User);

        locale = Locale.of("ru");

        List<Category> rootCategories = categoryService.getRootCategories();
        model.addAttribute("rootCategories", rootCategories);

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("activePage", "admin-categories");
        return "admin/categories/categories-list";
    }

    @PostMapping("/add")
    public String addCategory(@RequestParam("translations") String translationsRaw,
                                @RequestParam(value = "categoryName") String categoryName,
                                @RequestParam(value = "leaf", defaultValue = "false") Boolean leaf,
                                @RequestParam(value = "parentCategoryId", required = false) Long parentCategoryId,
                                @AuthenticationPrincipal OAuth2User oauth2User,
                                RedirectAttributes redirectAttributes) {
        try {
            List<String> translations = Arrays.asList(translationsRaw.split(","));
            CategoryDTO categoryDto = new CategoryDTO();
            categoryDto.setName(categoryName);
            categoryDto.setLeaf(leaf);
            categoryDto.setParentId(parentCategoryId);
            System.out.println("Начинаем создавать категорию");
            categoryService.createCategory(categoryDto, translations);
            redirectAttributes.addFlashAttribute("successMessage", "Локация добавлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении локации");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes, @AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            if (!roleCheckService.hasRoleAdmin(userService.findUser(oAuth2User.getAttribute("email"), SearchParamType.EMAIL))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении локации, вы не являетесь админом");
                return "redirect:/error";
            }
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Локация успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении локации");
        }
        return "redirect:/admin/categories";
    }
}
