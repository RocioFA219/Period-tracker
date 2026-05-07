package com.example.period.tracker.infra.handler;

import com.example.period.tracker.domain.dto.ErrorResponse;
import com.example.period.tracker.domain.dto.UserRequestDTO;
import com.example.period.tracker.domain.dto.UserResponseDTO;
import com.example.period.tracker.domain.exception.UserNotFoundException;
import com.example.period.tracker.mapper.UserMapper;
import com.example.period.tracker.producer.UserEventProducer;
import com.example.period.tracker.respository.UserRepository;
import com.example.period.tracker.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class  UserHandler {
    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;
    private final KeycloakService keycloakService;
    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UserRequestDTO.class)
                .flatMap(userRequestDTO ->
                        // Ejecutamos Keycloak primero
                        keycloakService.createInKeycloak(userRequestDTO)
                                .then(Mono.defer(() -> userRepository.save(UserMapper.toEntity(userRequestDTO))))
                                .flatMap(savedUser ->
                                        // Emitimos a Kafka y retornamos el usuario guardado
                                        userEventProducer.emitUserMessage(
                                                savedUser.getId(),
                                                savedUser.getAverageCycleLength(),
                                                savedUser.getUsername()
                                        ).thenReturn(savedUser)
                                )
                )
                // Aquí el flujo ya es claramente Mono<User>, ahora lo pasamos a ServerResponse
                .map(UserMapper::toDto)
                .flatMap(dto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(dto))
                .onErrorResume(e -> {
                    log.error("Fallo en el proceso de registro: ", e); // Ahora 'log' es de Slf4j
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue(new ErrorResponse("REGISTRATION_FAILED", e.getMessage(), LocalDateTime.now()));
                });
    }
    public Mono<ServerResponse> getAllUsers(ServerRequest serverRequest){
        return ServerResponse.ok()
                .body(userRepository.findAll()
                        .map(UserMapper::toDto), UserResponseDTO.class);
    }
    public Mono<ServerResponse> findByEmail(ServerRequest serverRequest) {
        // Extraemos el parametro "email" de la URl.
        String email = serverRequest.queryParam("email")
                .orElseThrow(() -> new IllegalArgumentException("Email obligatorio"));

        return userRepository.findByEmail(email)
                .map(UserMapper::toDto)
                .switchIfEmpty(Mono.error(new UserNotFoundException(email)))
                .flatMap(dto-> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto))
                .onErrorResume(IllegalArgumentException.class,e ->
                        ServerResponse.badRequest().bodyValue(new ErrorResponse("BAD_REQUEST",e.getMessage(), LocalDateTime.now())));

    }

}
