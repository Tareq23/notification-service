package com.tareq23.notificationservice.infrastructure.channel;

import com.tareq23.notificationservice.domain.dispatch.port.NotificationSendException;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationSender;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;


@Component
public class ChannelSenderFactory implements NotificationSender {

    private final Map<NotificationChannel, ChannelAdapter> adapters;

    public ChannelSenderFactory(WhatsAppSender whatsAppSender) {

        adapters = new EnumMap<>(NotificationChannel.class);
        adapters.put(NotificationChannel.WHATSAPP,   whatsAppSender);
    }

    @Override
    public String send(String recipient, NotificationChannel channel, String subject, String body) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(channel,   "channel must not be null");

        ChannelAdapter adapter = adapters.get(channel);
        if (adapter == null) {
            throw new NotificationSendException(
                    "ChannelSenderFactory",
                    "No adapter registered for channel: " + channel,
                    false);
        }
        return adapter.send(recipient, subject, body);
    }

    public interface ChannelAdapter {
        String send(String recipient, String subject, String body);
    }
}
