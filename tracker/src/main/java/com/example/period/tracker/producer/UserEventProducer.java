package com.example.period.tracker.producer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public static final String TOPIC = "user-events";

    public Mono<Void> emitUserMessage(Long userId, Integer averageCycleLength, String username){
        String message = "USER_CREATED:" + userId + ":" + averageCycleLength + ":" + username;

        return Mono.fromFuture(kafkaTemplate.send(TOPIC, message))
                .doOnSuccess(result -> log.info("MENSAJE ENVIADO A KAFKA: {}", message))
                .doOnError(ex -> log.error("Error al enviar mensaje: {}", ex.getMessage()))
                .then();
    }
}
