package org.kirya343.main.controller;

import java.util.List;

import org.kirya343.main.model.Task;
import org.kirya343.main.model.User;
import org.kirya343.main.model.User.Role;
import org.kirya343.main.repository.TaskRepository;
import org.kirya343.main.repository.UserRepository;
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

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String adminTasks(Model model, @AuthenticationPrincipal OAuth2User oAuth2User) {
        List<Task> tasks = taskRepository.findAll();
        List<User> executors = userRepository.findByRole(Role.ADMIN);
        model.addAttribute("tasks", tasks);
        model.addAttribute("executors", executors);
        model.addAttribute("activePage", "admin-tasks");
        return "admin/tasks";
    }
}