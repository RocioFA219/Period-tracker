package com.example.period.tracker.service;

import com.example.period.tracker.domain.dto.UserRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService {
    // WebClient para llamadas HTTP reactivas a la API de Keycloak
    private final WebClient keyclaokWebClient;

    // Bean de Keycloak Admin Client ya configurado con ClientCredentials
    // Se usa SOLO para obtener el token de acceso automáticamente
    private final Keycloak keycloak;

    /**
     * Crea un usuario en Keycloak llamando a su API Admin.
     *
     * Flujo:
     * 1. Obtiene un token de acceso del bean Keycloak (configurado con client_credentials)
     * 2. Envía POST a /admin/realms/period-tracker/users con el token como Authorization
     * 3. Si falla, lanza una excepción con el mensaje de error
     */
    public Mono<Void> createInKeycloak(UserRequestDTO dto){
        /**
         *  // Obtenemos el token de acceso usando el Keycloak Admin Client ya configurado
         *         // El token se genera automáticamente gracias al grant type CLIENT_CREDENTIALS
         *         String token = keycloak.tokenManager().getAccessToken().getToken();
         *
         *         return keyclaokWebClient.post()
         *                 .uri("/admin/realms/period-tracker/users")
         *                 // Adjuntamos el token como header de autorización
         *                 // Sin este header, Keycloak rechazaría la petición con 401 Unauthorized
         *                 .header("Authorization", "Bearer " + token)
         *                 .bodyValue(mapToKeycloakUser(dto))
         *                 .retrieve()
         *                 .onStatus(HttpStatusCode::isError,clientResponse ->
         *                         clientResponse.bodyToMono(String.class)
         *                                 .flatMap(error -> Mono.error(new RuntimeException("Keycloak error: " +error)))
         *                 )
         *                 .toBodilessEntity()
         *                 .doOnSuccess(v-> log.info("Usuario creado en Keycloak"))
         *                 .then();
         */
        return Mono.fromCallable(() -> keycloak.tokenManager().getAccessToken().getToken())
                .subscribeOn(Schedulers.boundedElastic()) // Ejecuta en hilo separado para no bloquear el EventLoop
                .flatMap(token ->
                        keyclaokWebClient.post()
                                .uri("/admin/realms/period-tracker/users")
                                .header("Authorization", "Bearer " + token)
                                .bodyValue(mapToKeycloakUser(dto))
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, clientResponse ->
                                        clientResponse.bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(new RuntimeException("Keycloak error: " + error)))
                                )
                                .toBodilessEntity()
                )
                .doOnSuccess(v -> log.info("Usuario {} creado en Keycloak correctamente", dto.username()))
                .then();

    }

    private Map<String, Object> mapToKeycloakUser(UserRequestDTO dto) {
        Map<String, Object> representation = new HashMap<>();
        representation.put("username", dto.username());
        representation.put("email", dto.email());
        representation.put("enabled", true);

        if (dto.firstName() != null) {
            representation.put("firstName", dto.firstName());
        }
        if (dto.lastName() != null) {
            representation.put("lastName", dto.lastName());
        }

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", dto.password());
        credential.put("temporary", false);
        representation.put("credentials", List.of(credential));

        return representation;
    }
}
