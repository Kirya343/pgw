package org.kirya343.admin.controller;

import org.kirya343.main.model.News;
import org.kirya343.main.services.NewsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final NewsService newsService;

    @GetMapping
    public String newsList(Model model) {
        List<News> newsList = newsService.findAll();
        model.addAttribute("newsList", newsList);
        model.addAttribute("activePage", "admin-news");
        return "admin/news/news-list";
    }

    @GetMapping("/create")
    public String createNewsForm(Model model) {
        News news = new News();
        news.setPublishDate(LocalDateTime.now());
        model.addAttribute("news", news);
        model.addAttribute("activePage", "admin-news");
        return "admin/news/news-edit";
    }

    @PostMapping("/create")
    public String createNews(
        @ModelAttribute News news,
        @RequestParam(value = "titleRu", required = false) String titleRu,
        @RequestParam(value = "titleFi", required = false) String titleFi,
        @RequestParam(value = "titleEn", required = false) String titleEn,
        @RequestParam(value = "contentRu", required = false) String contentRu,
        @RequestParam(value = "contentFi", required = false) String contentFi,
        @RequestParam(value = "contentEn", required = false) String contentEn,
        @RequestParam(value = "excerptRu", required = false) String excerptRu,
        @RequestParam(value = "excerptFi", required = false) String excerptFi,
        @RequestParam(value = "excerptEn", required = false) String excerptEn,
        @RequestParam(required = false) MultipartFile imageFile,
        @RequestParam(required = false, defaultValue = "false") boolean removeImage,
        @AuthenticationPrincipal OAuth2User oauth2User,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Устанавливаем данные на основе параметров
            news.setTitleRu(titleRu);
            news.setTitleFi(titleFi);
            news.setTitleEn(titleEn);
            news.setContentRu(contentRu);
            news.setContentFi(contentFi);
            news.setContentEn(contentEn);
            news.setExcerptRu(excerptRu);
            news.setExcerptFi(excerptFi);
            news.setExcerptEn(excerptEn);

            news.setPublishDate(LocalDateTime.now());
            news.setPublished(true); // Или false, если ты хочешь добавить модерацию

            // Получаем имя автора
            String authorName = oauth2User.getAttribute("name");
            news.setAuthor(authorName != null ? authorName : "Админ");

            newsService.save(news, imageFile, removeImage);
            redirectAttributes.addFlashAttribute("successMessage", "Новость успешно создана");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании новости: " + e.getMessage());
        }

        return "redirect:/admin/news";
    }

    @GetMapping("/edit/{id}")
    public String editNewsForm(@PathVariable Long id, Model model) {
        try {
            News news = newsService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Новость не найдена"));
            model.addAttribute("news", news);
            model.addAttribute("activePage", "admin-news");
            return "admin/news/news-edit";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/news";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateNews(@PathVariable Long id,
                           @ModelAttribute News news,
                           @RequestParam(required = false) MultipartFile imageFile,
                           @RequestParam(required = false, defaultValue = "false") boolean removeImage,
                           @AuthenticationPrincipal OAuth2User oauth2User,
                           RedirectAttributes redirectAttributes) {
        try {
            news.setId(id);
            String authorName = oauth2User.getAttribute("name");
            news.setAuthor(authorName != null ? authorName : "Админ");
            newsService.save(news, imageFile, removeImage);
            redirectAttributes.addFlashAttribute("successMessage", "Новость успешно обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении новости");
        }
        return "redirect:/admin/news";
    }

    @GetMapping("/delete/{id}")
    public String deleteNews(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Новость успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении новости");
        }
        return "redirect:/admin/news";
    }
}