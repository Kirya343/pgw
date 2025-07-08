package org.kirya343.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.kirya343.main.exceptions.UserNotRegisteredException;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.UserRepository;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.util.unit.DataSize;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/admin/**").hasRole("ADMIN")
                            .requestMatchers("/secure/**").authenticated()
                            .requestMatchers("/uploads/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/users/accept-terms").authenticated()
                            .requestMatchers(HttpMethod.POST, "/register").permitAll()
                            .anyRequest().permitAll();
                })
                .oauth2Login(oauth2 -> {
                    oauth2.loginPage("/login")
                            .userInfoEndpoint(userInfo -> userInfo
                                    .oidcUserService(oidcUserCreate())
                            )
                            .successHandler((request, response, authentication) -> {
                                savedRequestAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
                            })
                            .failureHandler((request, response, exception) -> {
                                if (exception instanceof UserNotRegisteredException || 
                                    (exception.getCause() != null && exception.getCause() instanceof UserNotRegisteredException)) {
                                    
                                    // Получаем email из исключения
                                    String email = exception instanceof UserNotRegisteredException 
                                        ? ((UserNotRegisteredException) exception).getEmail()
                                        : ((UserNotRegisteredException) exception.getCause()).getEmail();
                                    
                                    // Перенаправляем с сохранением сессии
                                    response.sendRedirect("/register?error=user_not_registered&email=" 
                                        + URLEncoder.encode(email, StandardCharsets.UTF_8));
                                } else {
                                    response.sendRedirect("/login?error=oauth_error");
                                }
                            });
                })
                .formLogin(form -> {
                    form.loginPage("/login")
                            .successHandler((request, response, authentication) -> {
                                // Проверяем, является ли пользователь админом
                                if (authentication.getAuthorities().stream()
                                        .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"))) {
                                }
                                savedRequestAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
                            })
                            .failureUrl("/login?error")
                            .permitAll();
                })
                .logout(logout -> logout
                    .logoutUrl("/logout") // URL для logout
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                )
                .csrf(csrf -> {
                    csrf.ignoringRequestMatchers(
                        "/api/applications/**",
                        "/ws/**",
                        "/api/users/accept-terms"
                    );
                });
        return http.build();
    }

    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserCreate() {
        return userRequest -> {
            OidcUser oidcUser = new OidcUserService().loadUser(userRequest);
            String email = oidcUser.getEmail();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
            // Сохраняем данные пользователя в сессии перед выбросом исключения
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                request.getSession().setAttribute("oauth2User", oidcUser);
                throw new UserNotRegisteredException(email);
            }
            User user = optionalUser.get();
            Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }

    @Bean
    SavedRequestAwareAuthenticationSuccessHandler savedRequestAuthenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/catalog");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }

    @Bean
    public RequestCache requestCache() {
        return new HttpSessionRequestCache();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));
        return factory.createMultipartConfig();
    }

    @Bean
    ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
            sessionCookieConfig.setMaxAge(14 * 24 * 60 * 60); // 14 дней в секундах
        };
    }
}