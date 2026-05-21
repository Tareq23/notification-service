package com.tareq23.notificationservice.domain.dispatch.port;

import com.tareq23.notificationservice.domain.dispatch.aggregate.IdempotencyKey;


public interface IdempotencyRepository {

    boolean exists(IdempotencyKey key);

    void register(IdempotencyKey key, String notificationId);
}
