package org.kirya343.main.services.impl;

import org.kirya343.main.model.News;
import org.kirya343.main.repository.NewsRepository;
import org.kirya343.main.services.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.kirya343.main.services.NewsService;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private static final int DEFAULT_PAGE_SIZE = 5;
    private final NewsRepository newsRepository;
    private final StorageService storageService;

    // Основные CRUD операции
    @Override
    public List<News> findAll() {
        return newsRepository.findAll(Sort.by(Sort.Direction.DESC, "publishDate"));
    }

    @Override
    public Page<News> findAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    @Override
    public Optional<News> findById(Long id) {
        return newsRepository.findById(id);
    }

    @Override
    public News save(News news) {
        if (news.getPublishDate() == null) {
            news.setPublishDate(LocalDateTime.now());
        }
        return newsRepository.save(news);
    }

    @Override
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
    @Override
    public News save(News news, MultipartFile imageFile, boolean removeImage) throws IOException {
        // Обработка изображения
        if (imageFile != null && !imageFile.isEmpty()) {
            // Удаляем старое изображение если было
            if (news.getImageUrl() != null) {
                storageService.deleteImage(news.getImageUrl());
            }
            // Сохраняем новое изображение
            String imageUrl = storageService.storeNewsImage(imageFile, news.getId());
            news.setImageUrl(imageUrl);
        } else if (removeImage && news.getImageUrl() != null) {
            storageService.deleteImage(news.getImageUrl());
            news.setImageUrl(null);
        }

        return save(news);
    }

    // Методы для публичной части
    @Override
    public Page<News> getPublishedNews(int page) {
        return newsRepository.findByPublishedTrueOrderByPublishDateDesc(
                PageRequest.of(page, DEFAULT_PAGE_SIZE)
        );
    }

    @Override
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
    @Override
    public long countAll() {
        return newsRepository.count();
    }

    @Override
    public long countPublished() {
        return newsRepository.countByPublishedTrue();
    }

    @Override
    public Page<News> findSimilarNews(News currentNews, Pageable pageable) {
        return newsRepository.findSimilarNews(currentNews.getId(), pageable);
    }
}