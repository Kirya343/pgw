package org.kirya343;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "org.kirya343")
@EnableScheduling
@EnableTransactionManagement
public class WorkSwapApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkSwapApplication.class, args);
    }
}