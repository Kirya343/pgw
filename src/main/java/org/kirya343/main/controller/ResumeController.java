package org.kirya343.main.controller;

import org.kirya343.main.model.Resume;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.ResumeRepository;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ListingService;
import org.kirya343.main.services.StorageService;
import org.kirya343.main.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/secure/resume")
public class ResumeController {
    private final ResumeRepository resumeRepository;
    private final UserService userService;
    private final StorageService storageService;
    private final AvatarService avatarService;

    public ResumeController(ResumeRepository resumeRepository, UserService userService,
                            StorageService storageService, AvatarService avatarService) {
        this.resumeRepository = resumeRepository;
        this.userService = userService;
        this.storageService = storageService;
        this.avatarService = avatarService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("languages");
    }

    @GetMapping
    public String showResume(Model model) {
        User user = userService.getCurrentUser();
        Optional<Resume> resumeOpt = resumeRepository.findByUser(user);

        if (resumeOpt.isEmpty()) {
            model.addAttribute("hasResume", false);
            model.addAttribute("resume", new Resume());
        } else {
            model.addAttribute("hasResume", true);
            model.addAttribute("resume", resumeOpt.get());
        }

        String avatarPath = avatarService.resolveAvatarPath(user);
        String name = user.getName();

        model.addAttribute("userName", name != null ? name : "Пользователь");
        model.addAttribute("avatarPath", avatarPath);
        model.addAttribute("user", user);
        return "secure/resume";
    }

    @PostMapping("/save")
    public String saveResume(@ModelAttribute Resume resume,
                             @RequestParam(value = "resumeFile", required = false) MultipartFile file,
                             @RequestParam("languages[]") List<String> languages,
                             @RequestParam("languageLevels[]") List<String> levels,
                             @RequestParam(value = "publish", defaultValue = "false") boolean publish,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getCurrentUser();
            Optional<Resume> existingResume = resumeRepository.findByUser(user);

            Resume resumeToSave;
            if (existingResume.isPresent()) {
                resumeToSave = existingResume.get();
                // Обновляем поля
                resumeToSave.setProfession(resume.getProfession());
                resumeToSave.setExperience(resume.getExperience());
                resumeToSave.setEducation(resume.getEducation());
                resumeToSave.setSkills(resume.getSkills());
                resumeToSave.setAbout(resume.getAbout());
                resumeToSave.setContacts(resume.getContacts());
            } else {
                resumeToSave = resume;
                resumeToSave.setUser(user);
            }

            // Обновляем файл если загружен новый
            if (file != null && !file.isEmpty()) {
                String fileName = storageService.storeFile(file);
                resumeToSave.setFilePath(fileName);
            }

            // Обновляем языки
            Map<String, String> languageMap = new HashMap<>();
            if (languages.size() != levels.size()) {
                redirectAttributes.addFlashAttribute("error", "Ошибка: количество языков и уровней не совпадает");
                return "redirect:/secure/resume";
            }
            for (int i = 0; i < languages.size(); i++) {
                languageMap.put(languages.get(i), levels.get(i));
            }

            // Очищаем старые данные и добавляем новые
            resumeToSave.getLanguages().clear();
            resumeToSave.getLanguages().putAll(languageMap);

            // (по желанию)
            //resumeToSave.setLanguagesForm(languages);
            //resumeToSave.setLanguageLevelsForm(levels);

            // Устанавливаем статус публикации
            resumeToSave.setPublished(publish);

            resumeRepository.save(resumeToSave);

            if (publish) {
                redirectAttributes.addFlashAttribute("success", "Резюме успешно опубликовано");
            } else {
                redirectAttributes.addFlashAttribute("success", "Резюме успешно сохранено");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/secure/resume";
    }
}