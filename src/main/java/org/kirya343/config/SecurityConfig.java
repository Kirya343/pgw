package org.kirya343.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.SessionCookieConfig;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN_EMAIL = "kkodolov40@gmail.com";
    private static final String ADMIN2_EMAIL = "maryglotova09@gmail.com";

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //System.out.println(GREEN + "Configuring SecurityFilterChain" + RESET);

        http
                .authorizeHttpRequests(auth -> {
                    //System.out.println(GREEN + "Setting authorization rules" + RESET);
                    auth.requestMatchers("/admin/**").hasRole("ADMIN")
                            .requestMatchers("/secure/**").authenticated()
                            .requestMatchers("/uploads/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/users/accept-terms").authenticated()
                            .requestMatchers(HttpMethod.POST, "/register").permitAll()
                            .anyRequest().permitAll();
                })
                .oauth2Login(oauth2 -> {
                    //System.out.println(GREEN + "Configuring OAuth2 login" + RESET);
                    oauth2.loginPage("/login")
                            .userInfoEndpoint(userInfo -> userInfo
                                    .userService(createOAuth2UserService())
                            )
                            .successHandler((request, response, authentication) -> {
                                // Принудительно проверяем и обновляем роль после аутентификации
                                OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                                String email = oauthUser.getAttribute("email");
                                if (ADMIN_EMAIL.equalsIgnoreCase(email) || ADMIN2_EMAIL.equalsIgnoreCase(email)) {
                                    Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(
                                            "ROLE_ADMIN", "ROLE_USER"
                                    );
                                    authentication = new OAuth2AuthenticationToken(
                                            oauthUser,
                                            authorities,
                                            ((OAuth2AuthenticationToken)authentication).getAuthorizedClientRegistrationId()
                                    );
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                }
                                savedRequestAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
                            })
                            .failureHandler((request, response, exception) -> {
                                //System.out.println(GREEN + "OAuth2 login failed: " + exception.getMessage() + RESET);
                                if (exception.getMessage().contains("user_not_registered")) {
                                    response.sendRedirect("/register?oauth2_error=user_not_registered");
                                } else {
                                    response.sendRedirect("/login?error");
                                }
                            });
                })
                .formLogin(form -> {
                    //System.out.println(GREEN + "Configuring form login" + RESET);
                    form.loginPage("/login")
                            .successHandler((request, response, authentication) -> {
                                // Проверяем, является ли пользователь админом
                                if (authentication.getAuthorities().stream()
                                        .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"))) {
                                    System.out.println(GREEN + "админ авторизовался" + RESET);
                                }
                                savedRequestAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
                            })
                            .failureUrl("/login?error")
                            .permitAll();
                })
                .logout(logout -> {
                    //System.out.println(GREEN + "Configuring logout" + RESET);
                    logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                            .logoutSuccessUrl("/")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID");
                })
                .csrf(csrf -> {
                    csrf.ignoringRequestMatchers(
                        "/api/applications/**",
                        "/ws/**",
                        "/api/users/accept-terms" // 🔹 добавлено
                    );
                });
        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> createOAuth2UserService() {
        return userRequest -> {
            // Используем стандартный сервис для получения пользователя
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            String email = oAuth2User.getAttribute("email");
            //System.out.println("Processing OAuth2 login for: " + email);

            // Форсированно назначаем роль для админского email
            Set<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());
            if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                //System.out.println("Granted ADMIN role to: " + email);
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            return new DefaultOAuth2User(
                    authorities,
                    oAuth2User.getAttributes(),
                    "email" // name attribute key
            );
        };
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAuthenticationSuccessHandler() {
        //System.out.println(GREEN + "Creating SavedRequestAwareAuthenticationSuccessHandler" + RESET);
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/catalog");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        //System.out.println(GREEN + "Creating MultipartConfigElement" + RESET);
        return new MultipartConfigElement("", 10 * 1024 * 1024, 10 * 1024 * 1024, 0);
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
            sessionCookieConfig.setMaxAge(14 * 24 * 60 * 60); // 14 дней в секундах
        };
    }
}