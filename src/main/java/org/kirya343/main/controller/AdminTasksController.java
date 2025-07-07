package org.kirya343.main.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.kirya343.main.model.Task;
import org.kirya343.main.model.Task.Status;
import org.kirya343.main.model.Task.TaskType;
import org.kirya343.main.model.User;
import org.kirya343.main.model.User.Role;
import org.kirya343.main.repository.TaskRepository;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.UserService;
import org.kirya343.main.services.components.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/tasks")
public class AdminTasksController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserService userService;

    @GetMapping
    public String adminTasks(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        authService.validateAndAddAuthentication(model, oauth2User);

        List<Task> tasks = taskRepository.findAll();
        List<User> executors = userRepository.findByRole(Role.ADMIN);

        model.addAttribute("tasks", tasks);
        model.addAttribute("taskTypes", TaskType.values());
        model.addAttribute("executors", executors);
        model.addAttribute("activePage", "admin-tasks");
        return "admin/tasks";
    }

    @PostMapping("/create")
    public String createTask(@AuthenticationPrincipal OAuth2User oauth2User,
                                @RequestParam String taskName,
                                @RequestParam String taskDescription,
                                @RequestParam String taskType,
                                @RequestParam LocalDateTime deadline,
                                Model model) {
        Task task = new Task();
        task.setName(taskName);
        task.setDescription(taskDescription);
        task.setTaskType(TaskType.valueOf(taskType));
        task.setStatus(Status.NEW);
        task.setDeadline(deadline);
        task.setAuthor(userService.findUserFromOAuth2(oauth2User));

        taskRepository.save(task);

        return "redirect:/admin/tasks";
    }

    @PostMapping("/{id}/update")
    public String updateTask(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        return "redirect:/admin/tasks";
    }

    @PostMapping("/{id}/pickup")
    public String pickupTask(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));

        task.setExecutor(userService.findUserFromOAuth2(oauth2User));
        task.setStatus(Status.IN_PROGRESS);

        taskRepository.save(task);

        return "redirect:/admin/tasks";
    }

    @PostMapping("/{id}/confirm")
    public String confirmTask(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        
        task.setStatus(Status.COMPLETED);
        task.setCompleted(LocalDateTime.now());

        taskRepository.save(task);
        return "redirect:/admin/tasks";
    }

    @PostMapping("/{id}/cancel")
    public String cancelTask(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Задача с id " + id + " не найдена"));
        
        task.setStatus(Status.CANCELED);

        taskRepository.save(task);
        return "redirect:/admin/tasks";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        return "redirect:/admin/tasks";
    }

    @GetMapping("/{id}/details")
    public String getTaskDetailsFragment(@PathVariable Long id, Model model) {
        System.out.println("Поиск деталей задачи id: " + id);
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("task", task);
        return "fragments/admin/tasks :: taskDetails"; // путь и имя фрагмента
    }
}