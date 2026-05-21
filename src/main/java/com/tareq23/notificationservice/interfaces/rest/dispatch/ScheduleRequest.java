package com.tareq23.notificationservice.interfaces.rest.dispatch;

import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;


public record ScheduleRequest(

        @NotBlank(message = "userId is required")
        String userId,

        @NotNull(message = "channel is required")
        NotificationChannel channel,

        NotificationPriority priority,

        @NotBlank(message = "templateKey is required")
        String templateKey,

        Map<String, String> payload,

        @NotBlank(message = "correlationId is required")
        String correlationId,

        @NotNull(message = "scheduledAt is required")
        @Future(message = "scheduledAt must be a future instant")
        Instant scheduledAt
) {}
