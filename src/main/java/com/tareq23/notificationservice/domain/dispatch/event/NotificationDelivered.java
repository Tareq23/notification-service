package com.tareq23.notificationservice.domain.dispatch.event;

import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.shared.DomainEvent;

import java.time.Instant;


public record NotificationDelivered(
        String notificationId,
        String userId,
        NotificationChannel channel,
        Instant occurredOn
) implements DomainEvent {
}
