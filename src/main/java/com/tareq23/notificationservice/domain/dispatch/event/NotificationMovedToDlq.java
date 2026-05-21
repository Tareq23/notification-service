package com.tareq23.notificationservice.domain.dispatch.event;

import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.shared.DomainEvent;

import java.time.Instant;


public record NotificationMovedToDlq(
        String notificationId,
        String userId,
        NotificationChannel channel,
        String reason,
        int totalAttempts,
        Instant occurredOn
) implements DomainEvent {
}
