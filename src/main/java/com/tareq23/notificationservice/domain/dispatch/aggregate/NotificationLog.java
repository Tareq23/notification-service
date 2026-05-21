package com.tareq23.notificationservice.domain.dispatch.aggregate;

import com.tareq23.notificationservice.domain.dispatch.valueobject.DeliveryStatus;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;

import java.time.Instant;
import java.util.Objects;



public final class NotificationLog {

    private final String logId;
    private final String notificationId;
    private final NotificationChannel channel;
    private final DeliveryStatus status;
    private final String providerMessageId;   // ID returned by SES / FCM / Twilio
    private final String failureReason;        // null when successful
    private final int attemptNumber;
    private final Instant loggedAt;

    private NotificationLog(Builder builder) {
        this.logId = Objects.requireNonNull(builder.logId);
        this.notificationId = Objects.requireNonNull(builder.notificationId);
        this.channel = Objects.requireNonNull(builder.channel);
        this.status = Objects.requireNonNull(builder.status);
        this.providerMessageId = builder.providerMessageId;
        this.failureReason = builder.failureReason;
        this.attemptNumber = builder.attemptNumber;
        this.loggedAt = builder.loggedAt != null ? builder.loggedAt : Instant.now();
    }


    public String logId()             { return logId; }
    public String notificationId()    { return notificationId; }
    public NotificationChannel channel() { return channel; }
    public DeliveryStatus status()    { return status; }
    public String providerMessageId() { return providerMessageId; }
    public String failureReason()     { return failureReason; }
    public int attemptNumber()        { return attemptNumber; }
    public Instant loggedAt()         { return loggedAt; }


    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String logId;
        private String notificationId;
        private NotificationChannel channel;
        private DeliveryStatus status;
        private String providerMessageId;
        private String failureReason;
        private int attemptNumber = 1;
        private Instant loggedAt;

        public Builder logId(String v)              { this.logId = v; return this; }
        public Builder notificationId(String v)     { this.notificationId = v; return this; }
        public Builder channel(NotificationChannel v){ this.channel = v; return this; }
        public Builder status(DeliveryStatus v)     { this.status = v; return this; }
        public Builder providerMessageId(String v)  { this.providerMessageId = v; return this; }
        public Builder failureReason(String v)      { this.failureReason = v; return this; }
        public Builder attemptNumber(int v)         { this.attemptNumber = v; return this; }
        public Builder loggedAt(Instant v)          { this.loggedAt = v; return this; }

        public NotificationLog build() { return new NotificationLog(this); }
    }
}
