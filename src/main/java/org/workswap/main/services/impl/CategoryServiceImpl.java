package org.workswap.main.services.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.workswap.config.LocalisationConfig.LanguageUtils;
import org.workswap.datasource.main.model.DTOs.CategoryDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.listingModels.Category;
import org.workswap.datasource.main.repository.CategoryRepository;
import org.workswap.main.services.CategoryService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final MessageSource messageSource;

    @Override 
    public Category findCategory(String param, SearchParamType paramType) {
        switch (paramType) {
            case ID:
                return categoryRepository.findById(Long.parseLong(param)).orElse(null);
            default:
                throw new IllegalArgumentException("Unknown param type: " + paramType);
        }
    }

    @Override
    @Transactional
    public Category createCategory(CategoryDTO dto, List<String> translations) throws IOException {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists");
        }

        Category parent = null;
        if (dto.getParentId() != null) {
                        
            if (findCategory(dto.getParentId().toString(), SearchParamType.ID).isLeaf()) {
                throw new IllegalStateException("Cannot add subcategory to a leaf category");
            }
        }

        Category category = new Category(dto.getName(), parent);
        category.setLeaf(dto.isLeaf());

        if (translations != null) {
            for (String translation : translations) {
                String[] parts = translation.split("\\.");

                if (parts.length == 2) {
                    String text = parts[0];   // "перевод"
                    String lang = parts[1];   // "ru"
                    addCategoryTranslation(category.getName(), lang, text);
                } else {
                    // Обработка ошибки: неверный формат
                    throw new IllegalArgumentException("Неверный формат строки. Ожидалось: текст.язык");
                }
            }
        }

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
    public void deleteCategory(Long id)  {
        Category category = getCategoryById(id);
        
        if (!category.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }
        
        if (!category.getListings().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with associated listings");
        }

        for (String lang : LanguageUtils.SUPPORTED_LANGUAGES) {
            try {
                removeCategoryTranslation(category.getName(), lang);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    @Override
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    @Override
    public boolean isLeafCategory(Long categoryId) {
        if (categoryId == null) {
            return false;
        }
        
        return categoryRepository.findById(categoryId)
                .map(Category::isLeaf)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Категория с ID " + categoryId + " не найдена"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoryPath(Long categoryId) {

        if (categoryId == null) {
            return Collections.emptyList();
        }

        return categoryRepository.findCategoryPathWithNativeQuery(categoryId); 
    }

    @Override
    public CategoryDTO toDTO(Category category, Locale locale) {
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        System.out.println("Перевод категории " + category.getName() + ": " + messageSource.getMessage("category." + category.getName(), null, locale));
        return new CategoryDTO(category.getId(), category.getName(), parentId, category.isLeaf(), messageSource.getMessage("category." + category.getName(), null, locale));
    }

    @Override
    public void addCategoryTranslation(String categoryName, String lang, String translation) throws IOException {
        String key = "category." + categoryName;
        String lineToAdd = key + "=" + translation;

        // Формируем путь к файлу локализации
        String filename = "dinamic-lang/categories/categories_" + lang + ".properties";
        File file = new File(filename);

        // Убедимся, что файл существует
        if (!file.exists()) {
            file.getParentFile().mkdirs(); // Создаём папки, если нужно
            file.createNewFile();          // Создаём сам файл
        }

        // Загружаем все свойства из файла
        Properties props = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            props.load(reader);
        }

        // Проверяем, есть ли уже такой ключ
        if (props.containsKey(key)) {
            return; // Ничего не делаем — перевод уже есть
        }

        // Добавляем новую строку в конец файла
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            writer.newLine();
            writer.write(lineToAdd);
        }
    }

    public void removeCategoryTranslation(String categoryName, String lang) throws IOException {
        String key = "category." + categoryName;

        // Формируем путь к файлу локализации
        String filename = "dinamic-lang/categories/categories_" + lang + ".properties";
        File file = new File(filename);

        if (!file.exists()) {
            return; // Файл не существует — нечего удалять
        }

        // Загружаем свойства
        Properties props = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            props.load(reader);
        }

        // Удаляем ключ, если он есть
        if (!props.containsKey(key)) {
            return; // Ничего не делаем — ключа нет
        }

        props.remove(key);

        // Перезаписываем файл без удалённого ключа
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            props.store(writer, null); // Сохраняем обновлённые свойства
        }

        System.out.println("Успешно удалён перевод(" + lang + "): " + key);
    }

    @Override
    public List<Category> getAllDescendants(Category parent) {
        List<Category> descendants = new ArrayList<>();
        descendants.add(parent);
        List<Category> children = categoryRepository.findByParent(parent);
        for (Category child : children) {
            descendants.add(child);
            descendants.addAll(getAllDescendants(child));
            System.out.println("Дочерняя категория найдена: " + child.getName());
        }
        return descendants;
    }
}