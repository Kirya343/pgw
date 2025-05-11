package org.kirya343.main.services;

import org.kirya343.main.model.News;
import org.kirya343.main.repository.NewsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NewsService {
    private static final int DEFAULT_PAGE_SIZE = 5;
    private final NewsRepository newsRepository;
    private final StorageService storageService;

    public NewsService(NewsRepository newsRepository, StorageService storageService) {
        this.newsRepository = newsRepository;
        this.storageService = storageService;
    }

    // Основные CRUD операции
    public List<News> findAll() {
        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "publishDate"));
    }

    public Page<News> findAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    public Optional<News> findById(Long id) {
        return newsRepository.findById(id);
    }

    public News save(News news) {
        if (news.getPublishDate() == null) {
            news.setPublishDate(LocalDateTime.now());
        }
        return newsRepository.save(news);
    }

    public void deleteById(Long id) {
        newsRepository.findById(id).ifPresent(news -> {
            if (news.getImageUrl() != null) {
                try {
                    storageService.deleteImage(news.getImageUrl());
                } catch (IOException e) {
                    // Логирование ошибки удаления изображения
                }
            }
            newsRepository.delete(news);
        });
    }

    // Метод для сохранения с обработкой изображения
    public News save(News news, MultipartFile imageFile, boolean removeImage) throws IOException {
        // Обработка изображения
        if (imageFile != null && !imageFile.isEmpty()) {
            // Удаляем старое изображение если было
            if (news.getImageUrl() != null) {
                storageService.deleteImage(news.getImageUrl());
            }
            // Сохраняем новое изображение
            String imageUrl = storageService.storeImage(imageFile);
            news.setImageUrl(imageUrl);
        } else if (removeImage && news.getImageUrl() != null) {
            storageService.deleteImage(news.getImageUrl());
            news.setImageUrl(null);
        }

        return save(news);
    }

    // Методы для публичной части
    public Page<News> getPublishedNews(int page) {
        return newsRepository.findByPublishedTrueOrderByPublishDateDesc(
                PageRequest.of(page, DEFAULT_PAGE_SIZE)
        );
    }

    public List<News> findLatestPublishedNews(int count) {
    switch(count) {
        case 3: return newsRepository.findTop3ByPublishedTrueOrderByPublishDateDesc();
        // можно добавить другие варианты
        default: return newsRepository.findByPublishedTrueOrderByPublishDateDesc(
                    PageRequest.of(0, count))
                  .getContent();
    }
}

    // Статистические методы
    public long countAll() {
        return newsRepository.count();
    }

    public long countPublished() {
        return newsRepository.countByPublishedTrue();
    }

    public Page<News> findSimilarNews(News currentNews, Pageable pageable) {
        return newsRepository.findSimilarNews(currentNews.getId(), pageable);
    }
}