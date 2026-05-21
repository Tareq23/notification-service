package com.tareq23.notificationservice.domain.dispatch.port;

import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationLog;

import java.util.List;


public interface NotificationLogRepository {

    void save(NotificationLog log);

    void saveAll(List<NotificationLog> logs);

    List<NotificationLog> findLogsByNotificationId(String notificationId);

}
