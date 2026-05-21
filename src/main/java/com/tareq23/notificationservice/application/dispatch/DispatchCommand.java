package com.tareq23.notificationservice.application.dispatch;

import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationPriority;

import java.util.Map;

public record DispatchCommand(
        String userId,
        String recipient,
        NotificationChannel channel,
        NotificationPriority priority,
        String templateKey,
        String subject,
        String renderedBody,
        Map<String, String> payload,
        String correlationId
) {
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String userId;
        private String recipient;
        private NotificationChannel channel;
        private NotificationPriority priority = NotificationPriority.NORMAL;
        private String templateKey;
        private String subject = "";
        private String renderedBody;
        private Map<String, String> payload = Map.of();
        private String correlationId;

        public Builder userId(String v)                 { this.userId = v; return this; }
        public Builder recipient(String v)              { this.recipient = v; return this; }
        public Builder channel(NotificationChannel v)   { this.channel = v; return this; }
        public Builder priority(NotificationPriority v) { this.priority = v; return this; }
        public Builder templateKey(String v)            { this.templateKey = v; return this; }
        public Builder subject(String v)                { this.subject = v; return this; }
        public Builder renderedBody(String v)           { this.renderedBody = v; return this; }
        public Builder payload(Map<String, String> v)   { this.payload = v; return this; }
        public Builder correlationId(String v)          { this.correlationId = v; return this; }

        public DispatchCommand build() {
            return new DispatchCommand(userId, recipient, channel, priority,
                    templateKey, subject, renderedBody, payload, correlationId);
        }
    }
}
