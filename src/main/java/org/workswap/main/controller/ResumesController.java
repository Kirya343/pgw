package org.workswap.main.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.workswap.datasource.main.model.Resume;
import org.workswap.datasource.main.model.User;
import org.workswap.main.services.*;
import org.workswap.main.services.components.AuthService;
import org.workswap.main.services.components.AvatarService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumesController {

    private final ResumeService resumeService;
    private final UserService userService;
    private final AvatarService avatarService;
    private final AuthService authService;

    @GetMapping("/{id}")
    public String getResume(@PathVariable Long id, Model model,
                            @AuthenticationPrincipal OAuth2User oauth2User) {
        Resume resume = resumeService.getResumeByIdWithUser(id);
        User user = null;

        if (oauth2User != null) {
            user = userService.findUserFromOAuth2(oauth2User);
        }

        if (resume == null) {
            return "redirect:/catalog";
        }

        // Получаем автора объявления и его аватар
        User author = resume.getUser();

        authService.validateAndAddAuthentication(model, oauth2User);

        // Получаем пользователя резюме
        User candidate = resume.getUser();
        String candidateAvatarPath = avatarService.resolveAvatarPath(candidate);

        boolean isOwner = false;
        if (user != null) {
            isOwner = candidate != null && candidate.getId().equals(user.getId());
            model.addAttribute("isOwner", isOwner);
        }

        model.addAttribute("resume", resume);
        model.addAttribute("author", author.getName());
        model.addAttribute("sellerAvatarPath", candidateAvatarPath);

        return "resume";
    }
}