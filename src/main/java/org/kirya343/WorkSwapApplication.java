package org.kirya343;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class WorkSwapApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkSwapApplication.class, args);
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
        return "index"; // Вернёт index.html из templates/
    }
}