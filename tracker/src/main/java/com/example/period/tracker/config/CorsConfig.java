package com.example.period.tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Lista de orígenes permitidos (frontends de Vite)
        // Usamos una sola llamada para evitar sobrescribir la lista
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5174"
        ));

        corsConfig.setMaxAge(3600L);

        // Permitimos todos los métodos: GET, POST, PUT, DELETE, OPTIONS...
        corsConfig.setAllowedMethods(List.of("*"));

        // Permitimos todas las cabeceras incluyendo Authorization
        corsConfig.setAllowedHeaders(List.of("*"));

        // Permitimos enviar cookies y credenciales
        corsConfig.setAllowCredentials(true);

        // Registramos la configuración para todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
