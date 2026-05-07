package com.example.period.tracker.infra.handler;

import com.example.period.tracker.domain.dto.ErrorResponse;
import com.example.period.tracker.domain.exception.TrackerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TrackerException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTrackerException(TrackerException ex) {
        //Mapeo logico de codigos de error a estados HTTP
        HttpStatus status = switch (ex.getErrorCode()) {
            case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "EMAIL_DUPLICATED" -> HttpStatus.CONFLICT;
            case "INVALID_PASSWORD" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return Mono.just(ResponseEntity.status(status)
                .body(
                        new ErrorResponse(ex.getErrorCode(),
                                ex.getMessage(),
                                LocalDateTime.now())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        // Aquí podrías añadir un log para el desarrollador
        log.error("Error crítico inesperado atrapado por el ControllerAdvice: ", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "Un error inesperado ha ocurrido.", LocalDateTime.now())));
    }
}
