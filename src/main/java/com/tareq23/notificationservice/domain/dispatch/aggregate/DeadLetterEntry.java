package com.tareq23.notificationservice.domain.dispatch.aggregate;


import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;

public class DeadLetterEntry{

    private final String dlqId;
    private final String notificationId;
    private final UserId userId;
    private final NotificationChannel channel;
    private final String templateKey;
    private final String lastFailureReason;
    private final int totalAttempts;
    private final Instant firstAttemptAt;
    private final Instant movedToDlqAt;

    private DeadLetterEntry(Builder builder) {
        this.dlqId = Objects.requireNonNull(builder.dlqId);
        this.notificationId = Objects.requireNonNull(builder.notificationId);
        this.userId = Objects.requireNonNull(builder.userId);
        this.channel = Objects.requireNonNull(builder.channel);
        this.templateKey = Objects.requireNonNull(builder.templateKey);
        this.lastFailureReason = builder.lastFailureReason;
        this.totalAttempts = builder.totalAttempts;
        this.firstAttemptAt = Objects.requireNonNull(builder.firstAttemptAt);
        this.movedToDlqAt = builder.movedToDlqAt != null ? builder.movedToDlqAt : Instant.now();
    }


    public String dlqId()               { return dlqId; }
    public String notificationId()      { return notificationId; }
    public UserId userId()              { return userId; }
    public NotificationChannel channel(){ return channel; }
    public String templateKey()         { return templateKey; }
    public String lastFailureReason()   { return lastFailureReason; }
    public int totalAttempts()          { return totalAttempts; }
    public Instant firstAttemptAt()     { return firstAttemptAt; }
    public Instant movedToDlqAt()       { return movedToDlqAt; }


    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String dlqId;
        private String notificationId;
        private UserId userId;
        private NotificationChannel channel;
        private String templateKey;
        private String lastFailureReason;
        private int totalAttempts;
        private Instant firstAttemptAt;
        private Instant movedToDlqAt;

        public Builder dlqId(String v)                  { this.dlqId = v; return this; }
        public Builder notificationId(String v)         { this.notificationId = v; return this; }
        public Builder userId(UserId v)                 { this.userId = v; return this; }
        public Builder channel(NotificationChannel v)   { this.channel = v; return this; }
        public Builder templateKey(String v)            { this.templateKey = v; return this; }
        public Builder lastFailureReason(String v)      { this.lastFailureReason = v; return this; }
        public Builder totalAttempts(int v)             { this.totalAttempts = v; return this; }
        public Builder firstAttemptAt(Instant v)        { this.firstAttemptAt = v; return this; }
        public Builder movedToDlqAt(Instant v)          { this.movedToDlqAt = v; return this; }

        public DeadLetterEntry build() { return new DeadLetterEntry(this); }
    }
}
