package com.tareq23.notificationservice.domain.shared;


import java.time.Instant;


public interface DomainEvent {
    Instant occurredOn();
}
