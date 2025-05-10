package org.kirya343;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

@SpringBootApplication(scanBasePackages = "org.kirya343")
@EnableScheduling
@EnableTransactionManagement
public class WorkSwapApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(WorkSwapApplication.class, args);

        // Проверяем все бины OAuth2UserService
        Map<String, OAuth2UserService> oauthServices = ctx.getBeansOfType(OAuth2UserService.class);
        oauthServices.forEach((name, service) -> {
            System.out.println("OAUTH2 USER SERVICE BEAN: " + name + " -> " + service.getClass());
        });

        // Проверяем, какой сервис реально используется
        OAuth2UserService<?, ?> activeService = ctx.getBean(OAuth2UserService.class);
        System.out.println("ACTIVE OAUTH2 USER SERVICE: " + activeService.getClass());
    }
}