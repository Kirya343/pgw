package org.workswap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "org.workswap")
@EnableScheduling
@EnableTransactionManagement
public class WorkSwapApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkSwapApplication.class, args);
    }
}