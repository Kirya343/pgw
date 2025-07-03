package org.kirya343.main.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/tasks")
public class AdminTasksController {

    @GetMapping
    public String adminTasks(Model model, @AuthenticationPrincipal OAuth2User oAuth2User) {
        
        return "admin/tasks";
    }
}