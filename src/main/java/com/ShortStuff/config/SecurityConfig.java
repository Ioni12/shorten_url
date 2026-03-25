package com.ShortStuff.config;

import com.ShortStuff.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthService authService;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public SecurityConfig(JwtFilter jwtFilter, AuthService authService) {
        this.jwtFilter = jwtFilter;
        this.authService = authService;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/auth/google/callback").permitAll()
                        .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
                        .requestMatchers("/{code}").permitAll()
                        .requestMatchers("/health", "/time").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            org.springframework.security.oauth2.core.user.OAuth2User oAuth2User =
                                    (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
                            String email = oAuth2User.getAttribute("email");
                            String token = authService.loginOrRegisterGoogle(email);
                            response.sendRedirect(frontendUrl + "?token=" + token);
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins); // your frontend URL
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // apply to all routes
        return source;
    }
}