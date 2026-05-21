package com.tareq23.notificationservice.infrastructure.persistence.dispatch;

import com.tareq23.notificationservice.domain.dispatch.valueobject.DeliveryStatus;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLogDocument {

    private String logId;
    private String notificationId;
    private NotificationChannel channel;
    private DeliveryStatus status;
    private String providerMessageId;
    private String failureReason;
    private int attemptNumber;
    private Instant loggedAt;


}
