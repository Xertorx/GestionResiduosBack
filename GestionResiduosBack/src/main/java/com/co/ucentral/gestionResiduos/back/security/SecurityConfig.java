package com.co.ucentral.gestionResiduos.back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    // Endpoints públicos
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    
                    // GET EcoPoints - SIN JWT (público)
                    .requestMatchers(HttpMethod.GET, "/api/ecopoints/**").permitAll()
                    // POST, PUT, DELETE, PATCH EcoPoints - CON JWT (protegido)
                    .requestMatchers(HttpMethod.POST, "/api/ecopoints/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/ecopoints/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/ecopoints/**").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/ecopoints/**").authenticated()
                    
                    // POST Reportes - CON JWT (crear reporte requiere autenticación)
                    .requestMatchers(HttpMethod.POST, "/api/reportes/**").authenticated()
                    // PATCH Reportes (cambiar estado) - CON JWT (solo admin)
                    .requestMatchers(HttpMethod.PATCH, "/api/reportes/**").authenticated()
                    // GET Reportes - CON JWT (ver reportes requiere autenticación)
                    .requestMatchers(HttpMethod.GET, "/api/reportes/**").authenticated()
                    
                    // Resto de endpoints requieren autenticación
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}