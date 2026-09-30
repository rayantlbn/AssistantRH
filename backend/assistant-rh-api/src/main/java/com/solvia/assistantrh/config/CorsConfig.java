package com.solvia.assistantrh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autorise le frontend à appeler l'API depuis une autre origine (Vite : http://localhost:5173 en développement).
 * Origines lues dans app.cors.allowed-origins (liste séparée par des virgules), jamais codées en dur.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        // Lisibles par le frontend : URL de la ressource créée, nom du fichier téléchargé
                        .exposedHeaders("Location", "Content-Disposition");
            }
        };
    }
}
