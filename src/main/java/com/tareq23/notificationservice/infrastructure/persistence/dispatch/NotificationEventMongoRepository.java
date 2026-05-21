package com.tareq23.notificationservice.infrastructure.persistence.dispatch;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface NotificationEventMongoRepository
        extends MongoRepository<NotificationEventDocument, String> {

    Optional<NotificationEventDocument> findByIdempotencyKey(String idempotencyKey);
}
