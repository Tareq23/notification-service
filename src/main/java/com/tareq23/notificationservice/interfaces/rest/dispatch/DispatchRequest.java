package com.tareq23.notificationservice.interfaces.rest.dispatch;


import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;


public record DispatchRequest(

        @NotBlank(message = "userId is required")
        String userId,

        @NotBlank(message = "recipient is required")
        String recipient,

        @NotNull(message = "channel is required")
        NotificationChannel channel,

        NotificationPriority priority,

        @NotBlank(message = "templateKey is required")
        String templateKey,

        String subject,

        Map<String, String> payload,

        @NotBlank(message = "correlationId is required for idempotency")
        String correlationId
) {}
