package org.kirya343;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class WorkSwapApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkSwapApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "index"; // Вернёт index.html из templates/
    }
}