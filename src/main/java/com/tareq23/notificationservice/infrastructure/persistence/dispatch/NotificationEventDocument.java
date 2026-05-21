package com.tareq23.notificationservice.infrastructure.persistence.dispatch;

import com.tareq23.notificationservice.domain.dispatch.valueobject.DeliveryStatus;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationPriority;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Document(collection = "notification_events")
@Getter
@Setter
public class NotificationEventDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String idempotencyKey;

    private NotificationChannel channel;
    private NotificationPriority priority;
    private String templateKey;
    private Map<String, String> payload;

    private DeliveryStatus status;
    private String providerMessageId;
    private String failureReason;
    private int attemptNumber;

    private Instant scheduledAt;
    private Instant dispatchedAt;
    private Instant deliveredAt;
    private Instant createdAt;

    private List<NotificationLogDocument> logs;

}