package org.kirya343.main.controller;

import java.util.List;

import org.kirya343.main.model.Category;
import org.kirya343.main.model.DTOs.CategoryDTO;
import org.kirya343.main.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    @Autowired
    private CategoryRepository categoryRepository;

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
    
    // Другие методы: обновление, удаление и т.д.
}