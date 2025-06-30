package org.kirya343.main.controller;

import java.util.List;

import org.kirya343.main.model.Category;
import org.kirya343.main.model.DTOs.CategoryDTO;
import org.kirya343.main.repository.CategoryRepository;
import org.kirya343.main.services.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    // Создание новой категории
    @PostMapping
    public Category createCategory(@RequestBody CategoryDTO dto) {
        Category parent = dto.getParentId() != null ? 
            categoryRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent category not found")) 
            : null;
        
        Category category = new Category(dto.getName(), parent);
        category.setLeaf(dto.isLeaf());
        return categoryRepository.save(category);
    }

    // Получение дерева категорий
    @GetMapping("/tree")
    public List<Category> getCategoryTree() {
        return categoryRepository.findByParentIsNull(); // Корневые категории
    }
    
    @GetMapping("/roots")
    public List<Category> getRootCategories() {
        return categoryService.getRootCategories();
    }

    @ResponseBody
    @GetMapping("/children/{parentId}")
    public List<Category> getChildCategories(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getChildCategories(parentId);
        System.out.println("размер "+categories.size());
        return categories;
    }

    @GetMapping("/is-leaf/{categoryId}")
    public boolean isLeafCategory(@PathVariable Long categoryId) {
        return categoryService.isLeafCategory(categoryId);
    }

    @GetMapping("/path/{categoryId}")
    public List<Category> getCategoryPath(@PathVariable Long categoryId) {
        return categoryService.getCategoryPath(categoryId);
    }
}