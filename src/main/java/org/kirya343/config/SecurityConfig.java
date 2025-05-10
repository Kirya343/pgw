package org.kirya343.config;

import jakarta.servlet.MultipartConfigElement;
import org.kirya343.auth.CustomAuthenticationSuccessHandler;
import org.kirya343.auth.services.ForcedOAuth2UserService;
import org.kirya343.main.repository.UserRepository;
import org.kirya343.main.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final ForcedOAuth2UserService forcedOAuth2UserService;
//
//    public SecurityConfig(ForcedOAuth2UserService forcedOAuth2UserService) {
//        this.forcedOAuth2UserService = forcedOAuth2UserService;
//        System.out.println("SecurityConfig initialized with ForcedOAuth2UserService");
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/secure/**").authenticated()
//                        .anyRequest().permitAll()
//                )
//                .oauth2Login(oauth2 -> {
//                    oauth2.loginPage("/login");
//                    oauth2.userInfoEndpoint(userInfo -> {
//                        System.out.println("EXPLICITLY SETTING ForcedOAuth2UserService");
//                        userInfo.userService(forcedOAuth2UserService);
//                    });
//                    oauth2.successHandler((request, response, authentication) -> {
//                        System.out.println("AUTHENTICATION SUCCESS. AUTHORITIES: " + authentication.getAuthorities());
//                        response.sendRedirect("/secure/account");
//                    });
//                });
//
//        return http.build();
//    }
//}

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN_EMAIL = "kkodolov40@gmail.com";

    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private final ForcedOAuth2UserService forcedOAuth2UserService;
    private final UserService userService;
    private final UserRepository userRepository;

    public SecurityConfig(ForcedOAuth2UserService forcedOAuth2UserService, UserService userService, UserRepository userRepository) {
        this.forcedOAuth2UserService = forcedOAuth2UserService;
        this.userService = userService;
        this.userRepository = userRepository;
        System.out.println(RED + "SecurityConfig initialized" + RESET);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println(RED + "Configuring SecurityFilterChain" + RESET);

        http
                .authorizeHttpRequests(auth -> {
                    System.out.println(RED + "Setting authorization rules" + RESET);
                    auth.requestMatchers("/admin/**").hasRole("ADMIN")
                            .requestMatchers("/secure/**").authenticated()
                            .requestMatchers("/uploads/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/register").permitAll()
                            .anyRequest().permitAll();
                })
                .oauth2Login(oauth2 -> {
                    System.out.println(RED + "Configuring OAuth2 login" + RESET);
                    oauth2.loginPage("/login")
                            .userInfoEndpoint(userInfo -> userInfo
                                    .userService(createOAuth2UserService())
                            )
                            .successHandler((request, response, authentication) -> {
                                // Принудительно проверяем и обновляем роль после аутентификации
                                OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                                String email = oauthUser.getAttribute("email");
                                if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
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
                                System.out.println(RED + "OAuth2 login failed: " + exception.getMessage() + RESET);
                                if (exception.getMessage().contains("user_not_registered")) {
                                    response.sendRedirect("/register?oauth2_error=user_not_registered");
                                } else {
                                    response.sendRedirect("/login?error");
                                }
                            });
                })
                .formLogin(form -> {
                    System.out.println(RED + "Configuring form login" + RESET);
                    form.loginPage("/login")
                            .successHandler((request, response, authentication) -> {
                                // Проверяем, является ли пользователь админом
                                if (authentication.getAuthorities().stream()
                                        .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"))) {
                                    System.out.println(RED + "админ авторизовался" + RESET);
                                }
                                savedRequestAuthenticationSuccessHandler().onAuthenticationSuccess(request, response, authentication);
                            })
                            .failureUrl("/login?error")
                            .permitAll();
                })
                .logout(logout -> {
                    System.out.println(RED + "Configuring logout" + RESET);
                    logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                            .logoutSuccessUrl("/")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID");
                })
                .csrf(csrf -> {
                    System.out.println(RED + "Configuring CSRF ignoring for /api/applications/** and /ws/**" + RESET);
                    csrf.ignoringRequestMatchers("/api/applications/**", "/ws/**");
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
            System.out.println("Processing OAuth2 login for: " + email);

            // Форсированно назначаем роль для админского email
            Set<GrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities());
            if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                System.out.println("Granted ADMIN role to: " + email);
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
        System.out.println(RED + "Creating SavedRequestAwareAuthenticationSuccessHandler" + RESET);
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/catalog");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println(RED + "Creating PasswordEncoder (BCrypt)" + RESET);
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        System.out.println(RED + "Creating MultipartConfigElement" + RESET);
        return new MultipartConfigElement("", 5 * 1024 * 1024, 5 * 1024 * 1024, 0);
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        System.out.println(RED + "Creating InMemoryUserDetailsManager with admins" + RESET);
        UserDetails admin1 = User.builder()
                .username("kirya343")
                .password(encoder.encode("PGW353"))
                .roles("ADMIN")
                .build();

        UserDetails admin2 = User.builder()
                .username("testadmin")
                .password(encoder.encode("testadmin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin1, admin2);
    }

    @Bean
    public CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        System.out.println(RED + "Creating CustomAuthenticationSuccessHandler" + RESET);
        return new CustomAuthenticationSuccessHandler();
    }
}