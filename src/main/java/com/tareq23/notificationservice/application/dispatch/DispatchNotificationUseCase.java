package com.tareq23.notificationservice.application.dispatch;

import com.tareq23.notificationservice.domain.dispatch.aggregate.IdempotencyKey;
import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationEvent;
import com.tareq23.notificationservice.domain.dispatch.port.*;
import com.tareq23.notificationservice.domain.dispatch.valueobject.DeliveryStatus;
import com.tareq23.notificationservice.domain.dispatch.valueobject.EventPayload;
import com.tareq23.notificationservice.domain.shared.UserId;
import com.tareq23.notificationservice.infrastructure.messaging.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class DispatchNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(DispatchNotificationUseCase.class);

    private final NotificationEventRepository eventRepository;
    private final NotificationLogRepository logRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final NotificationSender notificationSender;
    private final DomainEventPublisher domainEventPublisher;

    public DispatchNotificationUseCase(
            NotificationEventRepository eventRepository,
            NotificationLogRepository logRepository,
            IdempotencyRepository idempotencyRepository,
            NotificationSender notificationSender,
            DomainEventPublisher domainEventPublisher) {
        this.eventRepository       = Objects.requireNonNull(eventRepository);
        this.logRepository         = Objects.requireNonNull(logRepository);
        this.idempotencyRepository = Objects.requireNonNull(idempotencyRepository);
        this.notificationSender    = Objects.requireNonNull(notificationSender);
        this.domainEventPublisher  = Objects.requireNonNull(domainEventPublisher);
    }

    @Transactional
    public DispatchResult dispatch(DispatchCommand command) {
        Objects.requireNonNull(command, "DispatchCommand must not be null");

        IdempotencyKey idempotencyKey = IdempotencyKey.from(
                command.userId(), command.templateKey(), command.correlationId());

        // Step 1: Idempotency check
        if (idempotencyRepository.exists(idempotencyKey)) {
            log.info("Duplicate dispatch request skipped. key={}", idempotencyKey);
            return DispatchResult.duplicate(idempotencyKey.value());
        }

        // Step 2: Create aggregate
        NotificationEvent event = NotificationEvent.builder()
                .userId(UserId.of(command.userId()))
                .idempotencyKey(idempotencyKey)
                .channel(command.channel())
                .priority(command.priority())
                .templateKey(command.templateKey())
                .payload(EventPayload.of(command.payload()))
                .build();

        // Step 3: Attempt send via provider
        try {
            String providerMessageId = notificationSender.send(
                    command.recipient(),
                    command.channel(),
                    command.subject(),
                    command.renderedBody()
            );
            event.markDispatched(providerMessageId);
            log.info("Notification dispatched. id={} channel={} provider={}",
                    event.notificationId(), command.channel(), providerMessageId);

        } catch (NotificationSendException ex) {
            event.markFailed(ex.getMessage());
            log.warn("Notification send failed. id={} retryable={} reason={}",
                    event.notificationId(), ex.isRetryable(), ex.getMessage());
        }

        // Step 4: Persist event + logs
        eventRepository.save(event);
        logRepository.saveAll(event.logs());

        // Step 5: Register idempotency key (only after successful persist)
        if (event.status() != DeliveryStatus.DUPLICATE) {
            idempotencyRepository.register(idempotencyKey, event.notificationId());
        }

        // Step 6: Publish domain events (Kafka)
        domainEventPublisher.publishAll(event.domainEvents());
        event.clearDomainEvents();

        return DispatchResult.from(event);
    }
}
