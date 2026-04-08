package com.co.ucentral.gestionResiduos.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<org.springframework.web.filter.OncePerRequestFilter> securityHeadersFilter() {
        org.springframework.boot.web.servlet.FilterRegistrationBean<org.springframework.web.filter.OncePerRequestFilter> filterBean =
                new org.springframework.boot.web.servlet.FilterRegistrationBean<>();

        filterBean.setFilter(new org.springframework.web.filter.OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response,
                    jakarta.servlet.FilterChain filterChain
            ) throws jakarta.servlet.ServletException, java.io.IOException {
                response.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
                response.setHeader("Cross-Origin-Embedder-Policy", "unsafe-none");
                filterChain.doFilter(request, response);
            }
        });

        filterBean.addUrlPatterns("/*");
        return filterBean;
    }
}