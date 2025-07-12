package org.workswap.main.services;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.workswap.datasource.main.model.News;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;

public interface NewsService {
    List<News> findAll();
    Page<News> findAll(Pageable pageable);

    News save(News news);
    void deleteById(Long id);
    News getNewsById(Long id);

    News findNews(String param, SearchParamType paramType);

    // Метод для сохранения с обработкой изображения
    News save(News news, MultipartFile imageFile, boolean removeImage) throws IOException;

    // Методы для публичной части
    Page<News> getPublishedNews(int page);

    List<News> findLatestPublishedNews(int count);

    // Статистические метод
    long countAll();
    long countPublished();
    Page<News> findSimilarNews(News currentNews, Pageable pageable);

    void localizeNews(News news, Locale locale);
    void localizeNewsIfLangPass(News news, Locale locale);
}
