package org.workswap.main.controller.secure;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.workswap.datasource.main.model.Resume;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.repository.ResumeRepository;
import org.workswap.main.services.StorageService;
import org.workswap.main.services.UserService;
import org.workswap.main.services.components.AuthService;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/secure/resume")
@RequiredArgsConstructor
public class OwnResumeController {
    private final ResumeRepository resumeRepository;
    private final UserService userService;
    private final StorageService storageService;
    private final AuthService authService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("languages");
    }

    @GetMapping
    public String showResume(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {

        User user = userService.findUserFromOAuth2(oauth2User);
        Optional<Resume> resumeOpt = resumeRepository.findByUser(user);

        if (resumeOpt.isEmpty()) {
            model.addAttribute("hasResume", false);
            model.addAttribute("resume", new Resume());
        } else {
            model.addAttribute("hasResume", true);
            model.addAttribute("resume", resumeOpt.get());
        }

        authService.validateAndAddAuthentication(model, oauth2User);

        // Переменная для отображения активной страницы
        model.addAttribute("activePage", "resume");

        return "secure/resume";
    }

    @PostMapping("/save")
    public String saveResume(@ModelAttribute Resume resume, 
                            @AuthenticationPrincipal OAuth2User oauth2User,
                            @RequestParam(value = "resumeFile", required = false) MultipartFile file,
                            @RequestParam("languages[]") List<String> languages,
                            @RequestParam("languageLevels[]") List<String> levels,
                            @RequestParam(value = "publish", defaultValue = "false") boolean publish,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findUserFromOAuth2(oauth2User);
            Optional<Resume> existingResume = resumeRepository.findByUser(user);

            Resume resumeToSave = existingResume.orElseGet(() -> {
                resume.setUser(user);
                return resume;
            });

            // Обновляем поля
            resumeToSave.setProfession(resume.getProfession());
            resumeToSave.setExperience(resume.getExperience());
            resumeToSave.setEducation(resume.getEducation());
            resumeToSave.setSkills(resume.getSkills());
            resumeToSave.setAbout(resume.getAbout());
            resumeToSave.setContacts(resume.getContacts());

            // Обновляем файл если загружен новый
            if (file != null && !file.isEmpty()) {
                // Удаляем старый файл, если он существует
                if (resumeToSave.getFilePath() != null) {
                    storageService.deleteFile(resumeToSave.getFilePath());
                }
                
                String fileName = storageService.storeResume(file, user.getId());
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

            resumeToSave.getLanguages().clear();
            resumeToSave.getLanguages().putAll(languageMap);
            resumeToSave.setPublished(publish);

            resumeRepository.save(resumeToSave);

            redirectAttributes.addFlashAttribute("success", 
                publish ? "Резюме успешно опубликовано" : "Резюме успешно сохранено");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/secure/resume";
    }
}