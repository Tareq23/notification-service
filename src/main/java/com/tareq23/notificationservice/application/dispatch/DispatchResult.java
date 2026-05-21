package com.tareq23.notificationservice.application.dispatch;

import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationEvent;
import com.tareq23.notificationservice.domain.dispatch.valueobject.DeliveryStatus;

public record DispatchResult(
        String notificationId,
        DeliveryStatus status,
        String providerMessageId,
        String failureReason,
        boolean duplicate
) {
    public static DispatchResult from(NotificationEvent event) {
        return new DispatchResult(
                event.notificationId(),
                event.status(),
                event.providerMessageId(),
                event.failureReason(),
                false
        );
    }

    public static DispatchResult duplicate(String idempotencyKey) {
        return new DispatchResult(null, DeliveryStatus.DUPLICATE, null,
                "Duplicate idempotency key: " + idempotencyKey, true);
    }

    public boolean isDispatched() {
        return status == DeliveryStatus.DISPATCHED;
    }

    public boolean isFailed() {
        return status == DeliveryStatus.FAILED;
    }
}
