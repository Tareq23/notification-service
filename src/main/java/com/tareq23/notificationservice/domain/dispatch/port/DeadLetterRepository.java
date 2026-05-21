package com.tareq23.notificationservice.domain.dispatch.port;

import com.tareq23.notificationservice.domain.dispatch.aggregate.DeadLetterEntry;

import java.util.List;
import java.util.Optional;


public interface DeadLetterRepository {

    void save(DeadLetterEntry entry);

    Optional<DeadLetterEntry> findDlqByNotificationId(String notificationId);

    List<DeadLetterEntry> findAll(int page, int size);

    void delete(String dlqId);
}
