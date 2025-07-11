package org.workswap.main.services;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.workswap.datasource.main.model.DTOs.CategoryDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.listingModels.Category;

public interface CategoryService {

    Category findCategory(String param, SearchParamType paramType);

    Category createCategory(CategoryDTO dto, List<String> translations) throws IOException;
    List<Category> getCategoryTree();
    Category getCategoryById(Long id);
    void deleteCategory(Long id);
    Category updateCategory(Long id, CategoryDTO dto);
    List<Category> getLeafCategories();
    List<Category> getChildCategories(Long parentId);
    List<Category> getRootCategories();
    boolean isLeafCategory(Long categoryId);
    List<Category> getCategoryPath(Long categoryId);
    CategoryDTO toDTO(Category category, Locale locale);

    //метод получения всех дочерних категорий
    List<Category> getAllDescendants(Category parent);

    // Метод для записи переводов категорий
    void addCategoryTranslation(String categoryName, String lang, String translation) throws IOException;
}