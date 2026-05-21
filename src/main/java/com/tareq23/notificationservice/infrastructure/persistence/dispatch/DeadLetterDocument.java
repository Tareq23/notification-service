package com.tareq23.notificationservice.infrastructure.persistence.dispatch;

import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document(collection = "notification_dlq")
@Getter
@Setter
public class DeadLetterDocument {

    @Id
    private String dlqId;

    @Indexed
    private String notificationId;

    @Indexed
    private String userId;

    private NotificationChannel channel;
    private String templateKey;
    private String lastFailureReason;
    private int totalAttempts;
    private Instant firstAttemptAt;
    private Instant movedToDlqAt;

}