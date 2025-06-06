package org.kirya343.main.controller;

import org.kirya343.main.model.News;
import org.kirya343.main.services.components.AuthService;
import org.kirya343.main.services.components.StatService;
import org.kirya343.main.services.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    private final StatService statService;
    private final AuthService authService;

    @GetMapping("/news")
    public String getNews(@RequestParam(defaultValue = "0") int page,
                         Locale locale,
                         Model model, 
                         @AuthenticationPrincipal OAuth2User oauth2User) {
        Page<News> newsPage = newsService.getPublishedNews(page);
        Map<String, Object> siteStats = statService.getSiteStats(locale);

        // Интегрируем логику локализации описания и названия
        for (News news : newsPage.getContent()) {
            String title = null;
            String excerpt = null;
            String content = null;

            if ("fi".equals(locale.getLanguage())) {
                title = news.getTitleFi();
                excerpt = news.getExcerptFi();
                content = news.getContentFi();
            } else if ("ru".equals(locale.getLanguage())) {
                title = news.getTitleRu();
                excerpt = news.getExcerptRu();
                content = news.getContentRu();
            } else if ("en".equals(locale.getLanguage())) {
                title = news.getTitleEn();
                excerpt = news.getExcerptEn();
                content = news.getContentEn();
            }

            // Сохраняем в транзиентные поля
            news.setLocalizedTitle(title);
            news.setLocalizedExcerpt(excerpt);
            news.setLocalizedContent(content);
        }

        authService.validateAndAddAuthentication(model, oauth2User);

        model.addAttribute("news", newsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("stats", siteStats);

        return "news";
    }

    @GetMapping("/news/{id}")
    public String viewNews(@PathVariable Long id,
                         @AuthenticationPrincipal OAuth2User oauth2User,
                         Locale locale,
                         Model model) {
        // Получаем новость по ID
        News news = newsService.findById(id)
            .orElseThrow();

        // Проверяем, является ли пользователь администратором
        boolean isAdmin = oauth2User != null && 
                        oauth2User.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        // Получаем 3 похожие новости
        Page<News> similarNews = newsService.findSimilarNews(news, PageRequest.of(0, 3));

        similarNews.forEach(similar -> setLocalizedTitleAndExcerptAndContent(similar, locale));

        // 3. Локализация названия и описания
        setLocalizedTitleAndExcerptAndContent(news, locale);

        // Добавляем атрибуты в модель
        model.addAttribute("news", news);
        model.addAttribute("similarNews", similarNews);
        model.addAttribute("isAdmin", isAdmin);
        
        authService.validateAndAddAuthentication(model, oauth2User);

        return "news-view";
    }

    private void setLocalizedTitleAndExcerptAndContent(News news, Locale locale) {
        String title = null;
        String excerpt = null;
        String content = null;

        if ("fi".equals(locale.getLanguage())) {
            title = news.getTitleFi();
            excerpt = news.getExcerptFi();
            content = news.getContentFi();
        } else if ("ru".equals(locale.getLanguage())) {
            title = news.getTitleRu();
            excerpt = news.getExcerptRu();
            content = news.getContentRu();
        } else if ("en".equals(locale.getLanguage())) {
            title = news.getTitleEn();
            excerpt = news.getExcerptEn();
            content = news.getContentEn();
        }

        news.setLocalizedTitle(title);
        news.setLocalizedExcerpt(excerpt);
        news.setLocalizedContent(content);
    }
}