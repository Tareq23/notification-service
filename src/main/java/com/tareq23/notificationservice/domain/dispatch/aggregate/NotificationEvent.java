package com.tareq23.notificationservice.domain.dispatch.aggregate;

import com.tareq23.notificationservice.domain.dispatch.event.NotificationDelivered;
import com.tareq23.notificationservice.domain.dispatch.event.NotificationDispatched;
import com.tareq23.notificationservice.domain.dispatch.event.NotificationFailed;
import com.tareq23.notificationservice.domain.dispatch.event.NotificationMovedToDlq;
import com.tareq23.notificationservice.domain.dispatch.valueobject.DeliveryStatus;
import com.tareq23.notificationservice.domain.dispatch.valueobject.EventPayload;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationPriority;
import com.tareq23.notificationservice.domain.shared.AggregateRoot;
import com.tareq23.notificationservice.domain.shared.UserId;

import java.time.Instant;
import java.util.*;

public class NotificationEvent extends AggregateRoot {

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final String notificationId;
    private final UserId userId;
    private final IdempotencyKey idempotencyKey;

    private final NotificationChannel channel;
    private final NotificationPriority priority;
    private final String templateKey;
    private final EventPayload payload;

    private DeliveryStatus status;
    private String providerMessageId;
    private String failureReason;
    private int attemptNumber;
    private Instant scheduledAt;
    private Instant dispatchedAt;
    private Instant deliveredAt;
    private final Instant createdAt;

    private final List<NotificationLog> logs = new ArrayList<>();


    private NotificationEvent(Builder builder) {
        this.notificationId  = builder.notificationId != null
                ? builder.notificationId : UUID.randomUUID().toString();
        this.userId          = Objects.requireNonNull(builder.userId);
        this.idempotencyKey  = Objects.requireNonNull(builder.idempotencyKey);
        this.channel         = Objects.requireNonNull(builder.channel);
        this.priority        = builder.priority != null ? builder.priority : NotificationPriority.NORMAL;
        this.templateKey     = Objects.requireNonNull(builder.templateKey);
        this.payload         = builder.payload != null ? builder.payload : EventPayload.empty();
        this.status          = DeliveryStatus.PENDING;
        this.attemptNumber   = 0;
        this.scheduledAt     = builder.scheduledAt;
        this.createdAt       = Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public void markSuppressed(String reason) {
        assertStatus(DeliveryStatus.PENDING);
        this.status = DeliveryStatus.SUPPRESSED;
        this.failureReason = reason;
        appendLog(DeliveryStatus.SUPPRESSED, null, reason);
    }


    public void markDuplicate() {
        assertStatus(DeliveryStatus.PENDING);
        this.status = DeliveryStatus.DUPLICATE;
        appendLog(DeliveryStatus.DUPLICATE, null, "Duplicate idempotency key");
    }


    public void markDispatched(String providerMessageId) {
        assertStatus(DeliveryStatus.PENDING);
        this.status = DeliveryStatus.DISPATCHED;
        this.providerMessageId = providerMessageId;
        this.dispatchedAt = Instant.now();
        this.attemptNumber++;
        appendLog(DeliveryStatus.DISPATCHED, providerMessageId, null);
        registerEvent(new NotificationDispatched(notificationId, userId.value(), channel, attemptNumber, dispatchedAt));
    }


    public void markDelivered(String providerMessageId) {
        if (status != DeliveryStatus.DISPATCHED) {
            throw new IllegalStateException(
                    "Cannot mark DELIVERED from status " + status + " for notification " + notificationId);
        }
        this.status = DeliveryStatus.DELIVERED;
        this.providerMessageId = providerMessageId;
        this.deliveredAt = Instant.now();
        appendLog(DeliveryStatus.DELIVERED, providerMessageId, null);
        registerEvent(new NotificationDelivered(notificationId, userId.value(), channel, deliveredAt));
    }


    public void markFailed(String reason) {
        if (status != DeliveryStatus.DISPATCHED && status != DeliveryStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot mark FAILED from status " + status + " for notification " + notificationId);
        }
        this.status = DeliveryStatus.FAILED;
        this.failureReason = reason;
        appendLog(DeliveryStatus.FAILED, null, reason);
        registerEvent(new NotificationFailed(notificationId, userId.value(), channel, reason, attemptNumber, Instant.now()));
    }

    /**
     * Reset to PENDING for the next retry attempt.
     * Guards against retrying beyond MAX_RETRY_ATTEMPTS — moves to DLQ instead.
     */
    public void prepareForRetry() {
        assertStatus(DeliveryStatus.FAILED);
        if (attemptNumber >= MAX_RETRY_ATTEMPTS) {
            moveToDeadLetter("Max retry attempts (" + MAX_RETRY_ATTEMPTS + ") exceeded");
            return;
        }
        this.status = DeliveryStatus.PENDING;
        this.failureReason = null;
    }

    /**
     * Moves this notification to the dead-letter state.
     * Raises NotificationMovedToDlq domain event.
     */
    public void moveToDeadLetter(String reason) {
        this.status = DeliveryStatus.DEAD_LETTERED;
        this.failureReason = reason;
        appendLog(DeliveryStatus.DEAD_LETTERED, null, reason);
        registerEvent(new NotificationMovedToDlq(notificationId, userId.value(), channel, reason, attemptNumber, Instant.now()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read accessors
    // ─────────────────────────────────────────────────────────────────────────

    public String notificationId()        { return notificationId; }
    public UserId userId()                { return userId; }
    public IdempotencyKey idempotencyKey(){ return idempotencyKey; }
    public NotificationChannel channel()  { return channel; }
    public NotificationPriority priority(){ return priority; }
    public String templateKey()           { return templateKey; }
    public EventPayload payload()         { return payload; }
    public DeliveryStatus status()        { return status; }
    public String providerMessageId()     { return providerMessageId; }
    public String failureReason()         { return failureReason; }
    public int attemptNumber()            { return attemptNumber; }
    public Instant scheduledAt()          { return scheduledAt; }
    public Instant dispatchedAt()         { return dispatchedAt; }
    public Instant deliveredAt()          { return deliveredAt; }
    public Instant createdAt()            { return createdAt; }
    public List<NotificationLog> logs()   { return Collections.unmodifiableList(logs); }

    public boolean isRetryable() {
        return status == DeliveryStatus.FAILED && attemptNumber < MAX_RETRY_ATTEMPTS;
    }

    public boolean isTerminal() {
        return status == DeliveryStatus.DELIVERED
                || status == DeliveryStatus.DEAD_LETTERED
                || status == DeliveryStatus.SUPPRESSED
                || status == DeliveryStatus.DUPLICATE;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void assertStatus(DeliveryStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Expected status " + expected + " but was " + status
                            + " for notification " + notificationId);
        }
    }

    private void appendLog(DeliveryStatus logStatus, String providerId, String reason) {
        logs.add(NotificationLog.builder()
                .logId(UUID.randomUUID().toString())
                .notificationId(notificationId)
                .channel(channel)
                .status(logStatus)
                .providerMessageId(providerId)
                .failureReason(reason)
                .attemptNumber(attemptNumber)
                .loggedAt(Instant.now())
                .build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────────────────────

    public static final class Builder {
        private String notificationId;
        private UserId userId;
        private IdempotencyKey idempotencyKey;
        private NotificationChannel channel;
        private NotificationPriority priority;
        private String templateKey;
        private EventPayload payload;
        private Instant scheduledAt;

        public Builder notificationId(String v)          { this.notificationId = v; return this; }
        public Builder userId(UserId v)                  { this.userId = v; return this; }
        public Builder idempotencyKey(IdempotencyKey v)  { this.idempotencyKey = v; return this; }
        public Builder channel(NotificationChannel v)    { this.channel = v; return this; }
        public Builder priority(NotificationPriority v)  { this.priority = v; return this; }
        public Builder templateKey(String v)             { this.templateKey = v; return this; }
        public Builder payload(EventPayload v)           { this.payload = v; return this; }
        public Builder scheduledAt(Instant v)            { this.scheduledAt = v; return this; }

        public NotificationEvent build() { return new NotificationEvent(this); }
    }

}
