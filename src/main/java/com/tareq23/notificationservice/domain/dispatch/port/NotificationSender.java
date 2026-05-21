package com.tareq23.notificationservice.domain.dispatch.port;


import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import org.springframework.stereotype.Component;


@Component
public interface NotificationSender {
    String send(String recipient, NotificationChannel channel, String subject, String body);
}
