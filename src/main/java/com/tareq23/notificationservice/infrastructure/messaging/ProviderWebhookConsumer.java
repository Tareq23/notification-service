package com.tareq23.notificationservice.infrastructure.messaging;

import com.tareq23.notificationservice.application.dispatch.DeliveryWebhookUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Component
//@ConditionalOnProperty(
//        name = "app.kafka.enabled",
//        havingValue = "true"
//)
public class ProviderWebhookConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProviderWebhookConsumer.class);

    private final DeliveryWebhookUseCase deliveryWebhookUseCase;
    private final ObjectMapper objectMapper;

    public ProviderWebhookConsumer(DeliveryWebhookUseCase deliveryWebhookUseCase,
                                   ObjectMapper objectMapper) {
        this.deliveryWebhookUseCase = Objects.requireNonNull(deliveryWebhookUseCase);
        this.objectMapper           = Objects.requireNonNull(objectMapper);
    }

    @KafkaListener(
            topics = "provider.delivery-receipt",
            groupId = "${spring.kafka.consumer.group-id:notification-service}",
            concurrency = "3"
    )
    public void onDeliveryReceipt(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);

            String notificationId   = node.path("notificationId").asText();
            String providerMessageId = node.path("providerMessageId").asText();
            String outcome          = node.path("outcome").asText();
            String reason           = node.path("reason").asText(null);
            String provider         = node.path("provider").asText("UNKNOWN");

            log.debug("Delivery receipt received. provider={} notificationId={} outcome={}",
                    provider, notificationId, outcome);

            if ("DELIVERED".equalsIgnoreCase(outcome)) {
                deliveryWebhookUseCase.onDelivered(notificationId, providerMessageId);

            } else if ("FAILED".equalsIgnoreCase(outcome)) {
                String failureReason = String.format("[%s] %s", provider,
                        reason != null ? reason : "Unknown failure");
                deliveryWebhookUseCase.onFailed(notificationId, failureReason);

            } else {
                log.warn("Unknown outcome in delivery receipt. outcome={} notificationId={}",
                        outcome, notificationId);
            }

        } catch (Exception ex) {
            log.error("Failed to process delivery receipt. message={} error={}", message, ex.getMessage(), ex);
        }
    }
}