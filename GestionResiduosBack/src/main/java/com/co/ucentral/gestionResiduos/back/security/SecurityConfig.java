package com.co.ucentral.gestionResiduos.back.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtFilter jwtFilter, ObjectMapper objectMapper) {
        this.jwtFilter = jwtFilter;
        this.objectMapper = objectMapper;
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
                        .requestMatchers("/uploads/**").permitAll()

                        // GET EcoPoints - SIN JWT (público)
                        .requestMatchers(HttpMethod.GET, "/api/ecopoints/**").permitAll()
                        // POST, PUT, DELETE, PATCH EcoPoints - CON JWT (protegido)
                        .requestMatchers(HttpMethod.POST, "/api/ecopoints/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/ecopoints/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/ecopoints/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/ecopoints/**").authenticated()

                        // POST Reportes - CON JWT (crear reporte requiere autenticación)
                        .requestMatchers(HttpMethod.POST, "/api/reports/**").authenticated()
                        // PATCH Reportes (cambiar estado) - CON JWT (solo admin)
                        .requestMatchers(HttpMethod.PATCH, "/api/reports/**").authenticated()
                        // GET Reportes - CON JWT (ver reportes requiere autenticación)
                        .requestMatchers(HttpMethod.GET, "/api/reports/**").authenticated()

                        // GET Categorías (todas, activas, por ID) - SIN JWT (público)
                        .requestMatchers(HttpMethod.GET, "/api/report-categories/**").permitAll()
                        // POST, PUT, DELETE, PATCH Categorías - CON JWT (admin)
                        .requestMatchers(HttpMethod.POST, "/api/report-categories/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/report-categories/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/report-categories/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/report-categories/**").authenticated()

                        // Usuarios - CON JWT (perfil propio)
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/uploads/**").permitAll()
                        // GET Calendarios por localidad - SIN JWT (público)
                        .requestMatchers(HttpMethod.GET, "/api/schedules/district/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/schedules/{id}").permitAll()
                        // GET todos, POST, PUT, DELETE, PATCH Calendarios - CON JWT (admin)
                        .requestMatchers(HttpMethod.GET, "/api/schedules").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/schedules/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/schedules/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/schedules/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/schedules/**").authenticated()

                        // Notificaciones
                        .requestMatchers(HttpMethod.GET, "/api/notifications/campaigns/active").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/notifications/campaigns/{id}").permitAll()
                        .requestMatchers("/api/notifications/**").authenticated()

                        // Contenidos educativos - SIN JWT (público)
                        // Permitir GET públicos para listar/ver contenidos sin requerir JWT
                        .requestMatchers(HttpMethod.GET, "/api/v1/education/**").permitAll()
                        // Feedback de contenidos educativos (POST feedback y GET stats) - PÚBLICO
                        .requestMatchers(HttpMethod.POST, "/api/v1/education/*/feedback").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/education/*/feedback/**").permitAll()

                        // Crear, editar, eliminar contenidos educativos - SOLO ADMINISTRADOR
                        .requestMatchers(HttpMethod.POST, "/api/v1/education/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/education/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/education/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/education/**").hasRole("ADMINISTRADOR")
                        // ── HU22: Quizzes interactivos ──
                        // GET público (jugar quiz, verificar existencia)
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/content/*/exists").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/content/*").permitAll()
                        // GET admin (ver respuestas correctas) → solo ADMINISTRADOR
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/content/*/admin").hasRole("ADMINISTRADOR")
                        // POST attempt → cualquier usuario autenticado
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/*/attempt").authenticated()
                        // POST/PUT/DELETE quizzes → solo ADMINISTRADOR
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/quizzes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/quizzes/**").hasRole("ADMINISTRADOR")

                        // ── HU31: Ranking de usuarios (público) ──
                        .requestMatchers(HttpMethod.GET, "/api/ranking/**").permitAll()
                        // ── Logros: requiere autenticación ──
                        .requestMatchers("/api/achievements/**").authenticated()
                        // Resto de endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                // Manejo de errores de autenticación (401) y acceso denegado (403) como JSON
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("timestamp", LocalDateTime.now().toString());
                            errorResponse.put("status", 401);
                            errorResponse.put("error", "Unauthorized");
                            errorResponse.put("message", "No estás autenticado. Debes enviar un token JWT válido en el header Authorization.");
                            errorResponse.put("path", request.getRequestURI());

                            objectMapper.writeValue(response.getOutputStream(), errorResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("timestamp", LocalDateTime.now().toString());
                            errorResponse.put("status", 403);
                            errorResponse.put("error", "Forbidden");
                            errorResponse.put("message", "No tienes permisos para acceder a este recurso.");
                            errorResponse.put("path", request.getRequestURI());

                            objectMapper.writeValue(response.getOutputStream(), errorResponse);
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}