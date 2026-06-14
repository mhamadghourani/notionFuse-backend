package com.mhmd.notion_fuse.security.config;

import com.mhmd.notion_fuse.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CRITICAL: Activate CORS processing within Spring Security
                // This connects your global CorsConfig bean to the security filter lifecycle!
                .cors(org.springframework.security.config.Customizer.withDefaults())

                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                // 2. CRITICAL: Allow all browser preflight OPTIONS handshakes anonymously
                                .requestMatchers(org.springframework.web.cors.CorsUtils::isPreFlightRequest).permitAll()
                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                                // Your existing rules:
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("/api/v1/oauth/callback/notion").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/api/v1/notion/**").authenticated()
                                .requestMatchers("/api/v1/oauth/**").authenticated()
                                .anyRequest().authenticated()
                )
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}