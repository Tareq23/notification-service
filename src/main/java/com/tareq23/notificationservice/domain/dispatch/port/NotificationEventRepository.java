package com.tareq23.notificationservice.domain.dispatch.port;


import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationEvent;

import java.util.List;
import java.util.Optional;


public interface NotificationEventRepository {

    void save(NotificationEvent event);

    Optional<NotificationEvent> findById(String notificationId);

    Optional<NotificationEvent> findByIdempotencyKey(String idempotencyKey);

    List<NotificationEvent> findFailedByUserId(String userId);

    void update(NotificationEvent event);

}
