package org.workswap.main.controller.components;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.workswap.datasource.main.model.DTOs.CategoryDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.listingModels.Category;
import org.workswap.datasource.main.repository.CategoryRepository;
import org.workswap.main.services.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class ApiCategoryController {
    
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    // Создание новой категории
    @PostMapping
    public Category createCategory(@RequestBody CategoryDTO dto) {
        Category parent = categoryService.findCategory(dto.getParentId().toString(), SearchParamType.ID);
        
        Category category = new Category(dto.getName(), parent);
        category.setLeaf(dto.isLeaf());
        return categoryRepository.save(category);
    }

    @GetMapping("/children/{parentId}")
    public List<CategoryDTO> getChildCategories(@PathVariable Long parentId, Locale locale) {
        return categoryService.getChildCategories(parentId).stream()
                .map(category -> categoryService.toDTO(category, locale))
                .collect(Collectors.toList());
    }

    @GetMapping("/is-leaf/{categoryId}")
    public boolean isLeafCategory(@PathVariable Long categoryId) {
        return categoryService.isLeafCategory(categoryId);
    }

    @GetMapping("/path/{categoryId}")
    public List<CategoryDTO> getCategoryPath(@PathVariable Long categoryId, Locale locale) {
        return categoryService.getCategoryPath(categoryId).stream()
                .map(category -> categoryService.toDTO(category, locale))
                .collect(Collectors.toList());
    }
}