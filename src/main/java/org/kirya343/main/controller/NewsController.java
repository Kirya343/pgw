package org.kirya343.main.controller;

import org.kirya343.main.model.News;
import org.kirya343.main.services.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;
import java.util.Map;

@Controller
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/news")
    public String getNews(@RequestParam(defaultValue = "0") int page,
                          Locale locale,
                          Model model) {
        Page<News> newsPage = newsService.getPublishedNews(page);
        Map<String, Object> siteStats = newsService.getSiteStats(locale);

        model.addAttribute("news", newsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("stats", siteStats);

        return "news";
    }
}
