package com.neusoft.cloudbrain.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/doc.html",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/favicon.ico",
                    "/",
                    "/api/auth/**",
                    "/api/patient/register",
                    "/api/patient/login",
                    "/api/patient/login/phone",
                    "/api/doctor/list",
                    "/api/doctor/detail",
                    "/api/doctor/login",
                    "/api/doctor/login/phone",
                    "/api/doctor/register",
                    "/api/doctor/status",
                    "/api/doctor/pending",
                    "/api/doctor/all-with-status",
                    "/api/doctor/approve",
                    "/api/doctor/reject",
                    "/api/triage/consult",
                    "/api/registration/create",
                    "/api/registration/list",
                    "/api/registration/cancel",
                    "/api/registration/cancel/**",
                    "/api/medical-record/generate",
                    "/api/medical-record/save",
                    "/api/medical-record/list",
                    "/api/medical-record/detail",
                    "/api/medical-record/detail/**",
                    "/api/prescription/create",
                    "/api/prescription/check",
                    "/api/prescription/check/**",
                    "/api/prescription/list",
                    "/api/prescription/detail",
                    "/api/prescription/detail/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
