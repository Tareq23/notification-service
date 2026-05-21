package com.tareq23.notificationservice.infrastructure.messaging;

import com.tareq23.notificationservice.domain.shared.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;


@Component
//@ConditionalOnProperty(
//        name = "app.kafka.enabled",
//        havingValue = "true"
//)
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);
    private static final String TOPIC_PREFIX = "notification.";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
        this.objectMapper  = Objects.requireNonNull(objectMapper);
    }


    public void publishAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) return;
        events.forEach(this::publish);
    }

    public void publish(DomainEvent event) {
        String topic = resolveTopic(event);
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish domain event. topic={} event={} error={}",
                                    topic, event.getClass().getSimpleName(), ex.getMessage());
                        } else {
                            log.debug("Domain event published. topic={} event={}",
                                    topic, event.getClass().getSimpleName());
                        }
                    });
        } catch (RuntimeException ex) {
            log.error("Failed to serialize domain event. event={} error={}",
                    event.getClass().getSimpleName(), ex.getMessage());
        }
    }


    private String resolveTopic(DomainEvent event) {
        String simpleName = event.getClass().getSimpleName();
        // CamelCase → kebab-case
        String kebab = simpleName
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase();
        return TOPIC_PREFIX + kebab;
    }
}
