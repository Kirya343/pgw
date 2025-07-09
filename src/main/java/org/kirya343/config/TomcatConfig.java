package org.kirya343.config;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.MultipartConfigElement;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class TomcatConfig {

    private final MultipartConfigElement multipartConfigElement;
    private final ServerProperties serverProperties;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(-1); // необязательно, но можно
            connector.setMaxSavePostSize(-1);
            connector.setProperty("fileCountLimit", "-1"); // главное — отключить лимит
        });
    }

    @PostConstruct
    public void logMultipartSettings() {
        System.out.println("======= MultipartConfigElement Settings =======");
        System.out.println("Max file size: " + multipartConfigElement.getMaxFileSize());
        System.out.println("Max request size: " + multipartConfigElement.getMaxRequestSize());
        System.out.println("File size threshold: " + multipartConfigElement.getFileSizeThreshold());
    }

    @PostConstruct
    public void logTomcatSettings() {
        ServerProperties.Tomcat tomcat = serverProperties.getTomcat();

        System.out.println("======= Tomcat Settings =======");
        System.out.println("Tomcat max-swallow-size: " + tomcat.getMaxSwallowSize());
        System.out.println("Tomcat max-part-count: " + tomcat.getMaxPartCount());
        System.out.println("Tomcat max-http-form-post-size: " + tomcat.getMaxHttpFormPostSize());
    }
}