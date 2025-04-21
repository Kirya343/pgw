package org.kirya343;

import jakarta.servlet.MultipartConfigElement;
import org.kirya343.main.services.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/secure/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "OIDC_USER")
                        .requestMatchers("/uploads/**").permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(savedRequestAuthenticationSuccessHandler()) // Используем кастомный обработчик
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService())
                        )
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(savedRequestAuthenticationSuccessHandler()) // Используем тот же обработчик
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/applications/**")
                );

        return http.build();
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAuthenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/catalog"); // URL по умолчанию, если не было сохраненного запроса
        handler.setAlwaysUseDefaultTargetUrl(false); // Используем сохраненный запрос
        return handler;
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new CustomOAuth2UserService();
    }

    // Остальные бины остаются без изменений
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(
                "", // Место для временного хранения
                5 * 1024 * 1024, // Макс. размер файла (5MB)
                5 * 1024 * 1024, // Макс. размер запроса (5MB)
                0 // Размер, после которого файл записывается на диск
        );
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails kirya343 = User.builder()
                .username("kirya343")
                .password(encoder.encode("PGW353"))
                .roles("ADMIN")
                .build();

        UserDetails testadmin = User.builder()
                .username("testadmin")
                .password(encoder.encode("testadmin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(kirya343, testadmin);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("877639477404-i3qsu3i9gbegu4fphjiagtu35ce3uc7f.apps.googleusercontent.com")
                .clientSecret("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")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                //disable  .redirectUri("https://test.globalworlds.net/login/oauth2/code/google")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientName("Google")
                .build();
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }
}
