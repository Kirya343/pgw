package org.workswap.main.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.workswap.datasource.main.model.News;
import org.workswap.datasource.main.model.NewsTranslation;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.DTOs.NewsForm;
import org.workswap.datasource.main.model.DTOs.NewsTranslationDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.main.services.NewsService;
import org.workswap.main.services.UserService;
import org.workswap.main.services.components.AuthService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final NewsService newsService;
    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public String newsList(Model model,
                           @AuthenticationPrincipal OAuth2User oauth2User) {
        authService.validateAndAddAuthentication(model, oauth2User);
        List<News> newsList = newsService.findAll();
        model.addAttribute("newsList", newsList);
        model.addAttribute("activePage", "admin-news");
        return "admin/news/news-list";
    }

    @GetMapping("/create")
    public String createNewsForm(@RequestParam(required = false, defaultValue = "false") boolean published, 
                                 Model model, 
                                 @AuthenticationPrincipal OAuth2User oauth2User) {
        authService.validateAndAddAuthentication(model, oauth2User);

        News news = new News();
        news.setPublishDate(LocalDateTime.now());
        model.addAttribute("news", news);
        model.addAttribute("newsForm", new NewsForm());
        model.addAttribute("published", published);
        model.addAttribute("activePage", "admin-news");
        return "admin/news/news-edit";
    }

    @PostMapping("/create")
    public String createNews(
        @ModelAttribute News news,
        @ModelAttribute NewsForm form,
        @RequestParam(required = false) MultipartFile imageFile,
        @RequestParam(required = false, defaultValue = "false") boolean removeImage,
        @RequestParam(required = false, defaultValue = "false") boolean published, //
        @AuthenticationPrincipal OAuth2User oauth2User,
        RedirectAttributes redirectAttributes
    ) {
        try {
            
            // Получаем новые переводы из формы
            Map<String, NewsTranslationDTO> translationDTOs = form.getTranslations();

            Map<String, NewsTranslation> newsTranslations = new HashMap<>();
            news.getCommunities().clear();

            for (Map.Entry<String, NewsTranslationDTO> entry : translationDTOs.entrySet()) {
                String lang = entry.getKey();
                NewsTranslationDTO dto = entry.getValue();

                System.out.println("LANG: " + lang + ", DTO: " + dto);

                NewsTranslation translation = new NewsTranslation();
                translation.setLanguage(lang);
                translation.setTitle(dto.getTitle());
                translation.setShortDescription(dto.getShortDescription());
                translation.setDescription(dto.getDescription());
                translation.setNews(news);

                newsTranslations.put(lang, translation);
                news.getCommunities().add(lang);
            }

            news.setTranslations(newsTranslations);

            news.setPublishDate(LocalDateTime.now());
            news.setPublished(published); // Или false, если ты хочешь добавить модерацию

            // Получаем имя автора
            User author = userService.findUserFromOAuth2(oauth2User);
            news.setAuthor(author);

            newsService.save(news, imageFile, removeImage);
            redirectAttributes.addFlashAttribute("successMessage", "Новость успешно создана");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании новости: " + e.getMessage());
        }

        return "redirect:/admin/news";
    }

    @GetMapping("/edit/{id}")
    public String editNewsForm(@PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "false") boolean published, 
                               Model model, 
                               @AuthenticationPrincipal OAuth2User oauth2User) throws JsonProcessingException {
        authService.validateAndAddAuthentication(model, oauth2User);
        try {
            News news = newsService.findNews(id.toString(), SearchParamType.ID);
            model.addAttribute("news", news);
            model.addAttribute("published", published);
            model.addAttribute("activePage", "admin-news");

            Map<String, Map<String, String>> translationsMap = new HashMap<>();
            news.getTranslations().forEach((lang, translation) -> {
                Map<String, String> data = new HashMap<>();
                data.put("title", translation.getTitle());
                data.put("shortDescription", translation.getShortDescription());
                data.put("description", translation.getDescription());
                translationsMap.put(lang, data);
            });


            ObjectMapper mapper = new ObjectMapper();
            String translationsJson = mapper.writeValueAsString(translationsMap);

            model.addAttribute("translationsJson", translationsJson);


            return "admin/news/news-edit";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/news";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateNews(@PathVariable Long id,
                           @ModelAttribute News news,
                           @ModelAttribute NewsForm form,
                           @RequestParam(required = false) MultipartFile imageFile,
                           @RequestParam(required = false, defaultValue = "false") boolean removeImage,
                           @RequestParam(required = false, defaultValue = "false") boolean published, 
                           @AuthenticationPrincipal OAuth2User oauth2User,
                           RedirectAttributes redirectAttributes) {
        try {
            // Получаем новые переводы из формы
            Map<String, NewsTranslationDTO> translationDTOs = form.getTranslations();

            Map<String, NewsTranslation> newsTranslations = new HashMap<>();
            news.getCommunities().clear();

            for (Map.Entry<String, NewsTranslationDTO> entry : translationDTOs.entrySet()) {
                String lang = entry.getKey();
                NewsTranslationDTO dto = entry.getValue();

                System.out.println("LANG: " + lang + ", DTO: " + dto);

                NewsTranslation translation = new NewsTranslation();
                translation.setLanguage(lang);
                translation.setTitle(dto.getTitle());
                translation.setShortDescription(dto.getShortDescription());
                translation.setDescription(dto.getDescription());
                translation.setNews(news);

                newsTranslations.put(lang, translation);
                news.getCommunities().add(lang);
            }

            news.setTranslations(newsTranslations);

            news.setPublishDate(LocalDateTime.now());
            news.setPublished(published); // Или false, если ты хочешь добавить модерацию

            // Получаем имя автора
            User author = userService.findUserFromOAuth2(oauth2User);
            news.setAuthor(author);

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