package com.tareq23.notificationservice.interfaces.rest.dispatch;

import com.tareq23.notificationservice.application.dispatch.DispatchResult;
import com.tareq23.notificationservice.domain.dispatch.valueobject.DeliveryStatus;


public record DispatchResponse(
        String notificationId,
        DeliveryStatus status,
        String providerMessageId,
        boolean duplicate,
        String message
) {
    public static DispatchResponse from(DispatchResult result) {
        String message = switch (result.status()) {
            case DISPATCHED    -> "Notification dispatched successfully.";
            case FAILED        -> "Notification failed to send. Retry scheduled.";
            case DUPLICATE     -> "Duplicate request — notification already processed.";
            case SUPPRESSED    -> "Recipient is suppressed. Notification not sent.";
            case DEAD_LETTERED -> "Notification moved to dead-letter queue.";
            default            -> result.status().name();
        };

        return new DispatchResponse(
                result.notificationId(),
                result.status(),
                result.providerMessageId(),
                result.duplicate(),
                message
        );
    }
}
