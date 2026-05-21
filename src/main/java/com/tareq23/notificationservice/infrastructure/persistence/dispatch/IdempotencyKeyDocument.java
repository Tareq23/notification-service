package com.tareq23.notificationservice.infrastructure.persistence.dispatch;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document(collection = "notification_idempotency_keys")
public class IdempotencyKeyDocument {

    @Id
    private String key;

    private String notificationId;

    @Indexed(expireAfter = "86400")
    private Instant createdAt;

    public String getKey()                  { return key; }
    public void setKey(String key)          { this.key = key; }

    public String getNotificationId()       { return notificationId; }
    public void setNotificationId(String v) { this.notificationId = v; }

    public Instant getCreatedAt()           { return createdAt; }
    public void setCreatedAt(Instant v)     { this.createdAt = v; }
}
