package com.tareq23.notificationservice.application.dispatch;

import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationEvent;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationEventRepository;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationLogRepository;
import com.tareq23.notificationservice.infrastructure.messaging.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
public class DeliveryWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeliveryWebhookUseCase.class);

    private final NotificationEventRepository eventRepository;
    private final NotificationLogRepository logRepository;
    private final DomainEventPublisher domainEventPublisher;

    public DeliveryWebhookUseCase(NotificationEventRepository eventRepository,
                                  NotificationLogRepository logRepository,
                                  DomainEventPublisher domainEventPublisher) {
        this.eventRepository       = Objects.requireNonNull(eventRepository);
        this.logRepository         = Objects.requireNonNull(logRepository);
        this.domainEventPublisher  = Objects.requireNonNull(domainEventPublisher);
    }


    @Transactional
    public void onDelivered(String notificationId, String providerMessageId) {
        NotificationEvent event = loadEvent(notificationId);
        event.markDelivered(providerMessageId);

        eventRepository.update(event);
        logRepository.saveAll(event.logs());
        domainEventPublisher.publishAll(event.domainEvents());
        event.clearDomainEvents();

        log.info("Delivery confirmed. id={} provider={}", notificationId, providerMessageId);
    }


    @Transactional
    public void onFailed(String notificationId, String reason) {
        NotificationEvent event = loadEvent(notificationId);
        event.markFailed(reason);

        eventRepository.update(event);
        logRepository.saveAll(event.logs());
        domainEventPublisher.publishAll(event.domainEvents());
        event.clearDomainEvents();

        log.warn("Delivery failure reported. id={} reason={}", notificationId, reason);
    }

    private NotificationEvent loadEvent(String notificationId) {
        return eventRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "NotificationEvent not found for webhook: " + notificationId));
    }
}
