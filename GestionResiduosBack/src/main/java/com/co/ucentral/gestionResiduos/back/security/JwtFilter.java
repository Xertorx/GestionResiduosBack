package com.co.ucentral.gestionResiduos.back.security;

import com.co.ucentral.gestionResiduos.back.exception.JwtAuthenticationException;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public JwtFilter(JwtService jwtService, ObjectMapper objectMapper, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            String email = jwtService.extractUsername(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Cargar rol del usuario para authorities
                List<SimpleGrantedAuthority> authorities = userRepository.findByEmail(email)
                        .map(user -> {
                            if (user.getRole() != null) {
                                return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
                            }
                            return List.<SimpleGrantedAuthority>of();
                        })
                        .orElse(Collections.emptyList());

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException e) {
            logger.warn("Error de autenticación JWT en {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getMessage(), request.getRequestURI());
        } catch (Exception e) {
            logger.error("Error inesperado procesando token JWT: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Error al procesar el token de autenticación", request.getRequestURI());
        }
    }

    /**
     * Envía una respuesta JSON de error directamente desde el filtro.
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}