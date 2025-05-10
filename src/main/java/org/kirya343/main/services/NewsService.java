package org.kirya343.main.services;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.News;
import org.kirya343.main.repository.ListingRepository;
import org.kirya343.main.repository.NewsRepository;
import org.kirya343.main.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class NewsService {
    private static final int DEFAULT_PAGE_SIZE = 5;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public NewsService(NewsRepository newsRepository,
                       UserRepository userRepository,
                       ListingRepository listingRepository) {
        this.newsRepository = newsRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    public Page<News> getPublishedNews(int page) {
        return newsRepository.findByPublishedTrueOrderByPublishDateDesc(
                PageRequest.of(page, DEFAULT_PAGE_SIZE)
        );
    }

    private String formatNumber(long number, Locale locale) {
        NumberFormat nf = NumberFormat.getInstance(locale);
        if (number >= 1_000_000) {
            return String.format(locale, "%.1fM+", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format(locale, "%.1fK+", number / 1_000.0);
        }
        return nf.format(number);
    }
}