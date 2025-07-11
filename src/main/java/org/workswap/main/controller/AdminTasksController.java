package org.workswap.main.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.workswap.datasource.main.model.Task;
import org.workswap.datasource.main.model.TaskComment;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.Task.Status;
import org.workswap.datasource.main.model.Task.TaskType;
import org.workswap.datasource.main.model.User.Role;
import org.workswap.datasource.main.repository.TaskCommentRepository;
import org.workswap.datasource.main.repository.TaskRepository;
import org.workswap.datasource.main.repository.UserRepository;
import org.workswap.main.services.TaksService;
import org.workswap.main.services.UserService;
import org.workswap.main.services.components.AuthService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/tasks")
public class AdminTasksController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserService userService;
    private final TaksService taksService;
    private final TaskCommentRepository taskCommentRepository;

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
        Task task = taksService.findTask(id.toString(), "ID");

        task.setExecutor(userService.findUserFromOAuth2(oauth2User));
        task.setStatus(Status.IN_PROGRESS);

        taskRepository.save(task);

        return "redirect:/admin/tasks";
    }

    @PostMapping("/{id}/confirm")
    public String confirmTask(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        Task task = taksService.findTask(id.toString(), "ID");
        
        task.setStatus(Status.COMPLETED);
        task.setCompleted(LocalDateTime.now());

        taskRepository.save(task);
        return "redirect:/admin/tasks";
    }

    @PostMapping("/{id}/cancel")
    public String cancelTask(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        Task task = taksService.findTask(id.toString(), "ID");
        
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
        Task task = taksService.findTask(id.toString(), "ID");
        model.addAttribute("task", task);
        model.addAttribute("status", task.getStatus().toString());
        return "fragments/admin/tasks :: taskDetails"; // путь и имя фрагмента
    }

    @GetMapping("/{id}/comments")
    public String getTaskComments(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        List<TaskComment> comments = taskCommentRepository.findAllByTaskId(id);
        model.addAttribute("comments", comments);
        model.addAttribute("admin", userService.findUserFromOAuth2(oauth2User));
        return "fragments/admin/tasks :: taskComments"; // путь и имя фрагмента
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<?> commentToTask(@PathVariable Long id,
                                            @RequestParam String commentContent, 
                                            Model model, 
                                            @AuthenticationPrincipal OAuth2User oauth2User) {
        Task task = taksService.findTask(id.toString(), "ID");

        TaskComment comment = new TaskComment();
        
        comment.setTask(task);
        comment.setAuthor(userService.findUserFromOAuth2(oauth2User));
        comment.setContent(commentContent);

        taskCommentRepository.save(comment);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/comment/delete")
    public String deleteCommentToTask(@PathVariable Long id,
                                            Model model, 
                                            @AuthenticationPrincipal OAuth2User oauth2User) {

        TaskComment comment = taskCommentRepository.findById(id).orElse(null);
        
        if (comment.getAuthor() != userService.findUserFromOAuth2(oauth2User)) {
            return "redirect:/admin/tasks";
        }

        taskCommentRepository.delete(comment);
        return "redirect:/admin/tasks";
    }
}