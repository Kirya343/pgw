package org.kirya343.main.services;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.kirya343.main.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface NewsService {
    List<News> findAll();
    Page<News> findAll(Pageable pageable);
    Optional<News> findById(Long id);
    News save(News news);
    void deleteById(Long id);

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
}
