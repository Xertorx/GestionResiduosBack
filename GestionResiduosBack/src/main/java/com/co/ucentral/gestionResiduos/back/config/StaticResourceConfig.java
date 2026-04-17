package com.co.ucentral.gestionResiduos.back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir fotos de perfil
        registry.addResourceHandler("/uploads/photo_profile/**")
                .addResourceLocations("file:uploads/photo_profile/");

        // Servir imágenes de reportes
        registry.addResourceHandler("/uploads/reports/**")
                .addResourceLocations("file:uploads/reports/");
    }
}

