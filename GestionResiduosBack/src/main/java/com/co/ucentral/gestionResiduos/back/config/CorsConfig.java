package com.co.ucentral.gestionResiduos.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite CORS para todas las rutas
                .allowedOriginPatterns("*") // Permite cualquier origen con patrón (compatible con allowCredentials)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Permite cualquier header
                .allowCredentials(true); // Permite credenciales (cookies, auth headers)
    }
}
