package org.kirya343.config;

import jakarta.servlet.MultipartConfigElement;
import org.kirya343.main.services.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService; // Важно!

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/secure/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "OIDC_USER")
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register").permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // <-- Внедрён через @Autowired
                        )
                        .successHandler(savedRequestAuthenticationSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            if (exception.getMessage().contains("user_not_registered")) {
                                // Просто редирект на регистрацию
                                response.sendRedirect("/register?oauth2_error=user_not_registered");
                            } else {
                                response.sendRedirect("/login?error");
                            }
                        })
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(savedRequestAuthenticationSuccessHandler())
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
                        .ignoringRequestMatchers("/ws/**")
                );

        return http.build();
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAuthenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/catalog");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(
                "",
                5 * 1024 * 1024,
                5 * 1024 * 1024,
                0
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
                //.redirectUri("http://localhost:8080/login/oauth2/code/google")
                .redirectUri("https://workswap.org/login/oauth2/code/google")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientName("Google")
                .build();
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }
}