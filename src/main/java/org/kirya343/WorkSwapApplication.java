package org.kirya343;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class WorkSwapApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkSwapApplication.class, args);
    }
}