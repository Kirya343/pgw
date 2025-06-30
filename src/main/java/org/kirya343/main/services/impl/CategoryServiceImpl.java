package org.kirya343.main.services.impl;

import java.util.List;

import org.kirya343.main.model.Category;
import org.kirya343.main.model.DTOs.CategoryDTO;
import org.kirya343.main.repository.CategoryRepository;
import org.kirya343.main.services.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists");
        }

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + dto.getParentId()));
            
            if (parent.isLeaf()) {
                throw new IllegalStateException("Cannot add subcategory to a leaf category");
            }
        }

        Category category = new Category(dto.getName(), parent);
        category.setLeaf(dto.isLeaf());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryTree() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        roots.forEach(this::loadChildrenRecursively);
        return roots;
    }

    private void loadChildrenRecursively(Category parent) {
        List<Category> children = categoryRepository.findByParentId(parent.getId());
        parent.setChildren(children);
        children.forEach(this::loadChildrenRecursively);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        
        if (!category.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }
        
        if (!category.getListings().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with associated listings");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryDTO dto) {
        Category category = getCategoryById(id);
        
        if (!category.getName().equals(dto.getName())) {
            if (categoryRepository.existsByName(dto.getName())) {
                throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists");
            }
            category.setName(dto.getName());
        }
        
        category.setLeaf(dto.isLeaf());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getLeafCategories() {
        return categoryRepository.findByLeaf(true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getChildCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }
}