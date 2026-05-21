package com.tareq23.notificationservice.domain.dispatch.event;

import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.shared.DomainEvent;

import java.time.Instant;


public record NotificationDispatched(
        String notificationId,
        String userId,
        NotificationChannel channel,
        int attemptNumber,
        Instant occurredOn
) implements DomainEvent {
}
