package com.example.period.tracker.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.server-url}")
    private String serverUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.admin-user:admin}")
    private String adminUser;
    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    /**
     * Bean de Keycloak Admin Client autenticado como admin.
     * Se usa para:
     * 1. Obtener tokens de acceso válidos
     * 2. Realizar operaciones administrativas (crear usuarios, asignar roles, etc.)
     */
    @Bean
    public Keycloak keycloak(){
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")  // Usamos el realm master para autenticación de admin
                .username(adminUser)
                .password(adminPassword)
                .clientId("admin-cli")  // Cliente por defecto para administración
                .build();
    }

    @Bean
    public WebClient keyclaokWebClient(@Value("${keycloak.server-url}") String serverUrl){
        return WebClient.builder()
                .baseUrl(serverUrl)
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }
}
