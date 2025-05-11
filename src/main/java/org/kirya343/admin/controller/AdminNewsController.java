package org.kirya343.admin.controller;

import org.kirya343.main.model.News;
import org.kirya343.main.services.NewsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final NewsService newsService;

    public AdminNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public String newsList(Model model) {
        List<News> newsList = newsService.findAll();
        model.addAttribute("newsList", newsList);
        return "admin/news/news-list";
    }

    @GetMapping("/create")
    public String createNewsForm(Model model) {
        News news = new News();
        news.setPublishDate(LocalDateTime.now());
        model.addAttribute("news", news);
        return "admin/news/news-edit";
    }

    @PostMapping("/create")
    public String createNews(@ModelAttribute News news,
                           @RequestParam(required = false) MultipartFile imageFile,
                           @RequestParam(required = false, defaultValue = "false") boolean removeImage,
                           RedirectAttributes redirectAttributes) {
        try {
            newsService.save(news, imageFile, removeImage);
            redirectAttributes.addFlashAttribute("successMessage", "Новость успешно создана");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании новости");
        }
        return "redirect:/admin/news";
    }

    @GetMapping("/edit/{id}")
    public String editNewsForm(@PathVariable Long id, Model model) {
        try {
            News news = newsService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Новость не найдена"));
            model.addAttribute("news", news);
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
                           RedirectAttributes redirectAttributes) {
        try {
            news.setId(id);
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