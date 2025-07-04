package org.kirya343.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Configuration
public class LocalisationConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
            "classpath:lang/messages",                     // старое местоположение
            "file:dinamic-lang/categories/categories"      // новое местоположение
        );
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // обновление кэша каждый час
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("locale");
        resolver.setDefaultLocale(Locale.forLanguageTag("ru"));
         resolver.setCookieMaxAge(Duration.ofDays(30));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // Параметр в URL, например: ?lang=fi
        return interceptor;
    }

    @Override
    public void addInterceptors(@SuppressWarnings("null") InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    public class LanguageUtils {
        public static final List<String> SUPPORTED_LANGUAGES = List.of("ru", "fi", "en", "it");
    }
}


