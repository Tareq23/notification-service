package com.tareq23.notificationservice.application.dispatch;


import com.tareq23.notificationservice.domain.dispatch.aggregate.IdempotencyKey;
import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationEvent;
import com.tareq23.notificationservice.domain.dispatch.port.IdempotencyRepository;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationEventRepository;
import com.tareq23.notificationservice.domain.dispatch.valueobject.EventPayload;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationPriority;
import com.tareq23.notificationservice.domain.shared.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Service
public class ScheduleNotificationUseCase {
    private static final Logger log = LoggerFactory.getLogger(ScheduleNotificationUseCase.class);

    private final NotificationEventRepository eventRepository;
    private final IdempotencyRepository idempotencyRepository;

    public ScheduleNotificationUseCase(NotificationEventRepository eventRepository,
                                       IdempotencyRepository idempotencyRepository) {
        this.eventRepository       = Objects.requireNonNull(eventRepository);
        this.idempotencyRepository = Objects.requireNonNull(idempotencyRepository);
    }

    @Transactional
    public String schedule(String userId,
                           NotificationChannel channel,
                           NotificationPriority priority,
                           String templateKey,
                           Map<String, String> payload,
                           String correlationId,
                           Instant scheduledAt) {

        IdempotencyKey key = IdempotencyKey.from(userId, templateKey, correlationId);
        if (idempotencyRepository.exists(key)) {
            log.info("Scheduled notification already exists, skipping. key={}", key);
            return null;
        }

        NotificationEvent event = NotificationEvent.builder()
                .userId(UserId.of(userId))
                .idempotencyKey(key)
                .channel(channel)
                .priority(priority)
                .templateKey(templateKey)
                .payload(EventPayload.of(payload))
                .scheduledAt(scheduledAt)
                .build();

        eventRepository.save(event);
        idempotencyRepository.register(key, event.notificationId());

        log.info("Notification scheduled. id={} at={}", event.notificationId(), scheduledAt);
        return event.notificationId();
    }
}
