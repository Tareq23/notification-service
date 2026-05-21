package com.tareq23.notificationservice.application.dispatch;

import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationEvent;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationEventRepository;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationLogRepository;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationSendException;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationSender;
import com.tareq23.notificationservice.infrastructure.messaging.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class RetryFailedNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedNotificationUseCase.class);

    private final NotificationEventRepository eventRepository;
    private final NotificationLogRepository logRepository;
    private final NotificationSender notificationSender;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public void retry(String notificationId, String recipient, String subject, String renderedBody) {
        NotificationEvent event = eventRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "NotificationEvent not found for retry: " + notificationId));

        // Aggregate decides if retry is allowed or DLQ transition is needed
        event.prepareForRetry();

        if (!event.isRetryable()) {
            // aggregate already transitioned to DEAD_LETTERED inside prepareForRetry()
            log.warn("Notification moved to DLQ after exhausting retries. id={}", notificationId);
            eventRepository.update(event);
            logRepository.saveAll(event.logs());
            domainEventPublisher.publishAll(event.domainEvents());
            event.clearDomainEvents();
            return;
        }

        try {
            String providerMessageId = notificationSender.send(
                    recipient, event.channel(), subject, renderedBody);
            event.markDispatched(providerMessageId);
            log.info("Retry succeeded. id={} attempt={} provider={}",
                    notificationId, event.attemptNumber(), providerMessageId);

        } catch (NotificationSendException ex) {
            event.markFailed(ex.getMessage());
            log.warn("Retry failed. id={} attempt={} reason={}",
                    notificationId, event.attemptNumber(), ex.getMessage());
        }

        eventRepository.update(event);
        logRepository.saveAll(event.logs());
        domainEventPublisher.publishAll(event.domainEvents());
        event.clearDomainEvents();
    }
}
