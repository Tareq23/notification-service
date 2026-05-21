package com.tareq23.notificationservice.infrastructure.channel;

import com.tareq23.notificationservice.domain.dispatch.port.NotificationSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Component
public class WhatsAppSender implements ChannelSenderFactory.ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppSender.class);

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String phoneNumberId;
    private final String accessToken;

    public WhatsAppSender(
            RestTemplate restTemplate,
            @Value("${notification.channel.whatsapp.api-url}")         String apiUrl,
            @Value("${notification.channel.whatsapp.phone-number-id}") String phoneNumberId,
            @Value("${notification.channel.whatsapp.access-token}")     String accessToken) {
        this.restTemplate  = restTemplate;
        this.apiUrl        = apiUrl;
        this.phoneNumberId = phoneNumberId;
        this.accessToken   = accessToken;
    }


    @Override
    public String send(String recipient, String subject, String body) {
        String endpoint = apiUrl + "/" + phoneNumberId + "/messages";

        // Strip leading '+' — WhatsApp API expects numbers without it
        String phone = recipient.startsWith("+") ? recipient.substring(1) : recipient;

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to",               phone,
                "type",             "text",
                "text",             Map.of("body", body)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(endpoint, new HttpEntity<>(payload, headers), Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body0 = response.getBody();
                @SuppressWarnings("unchecked")
                var messages = (java.util.List<Map<String, String>>) body0.get("messages");
                String wamid = messages != null && !messages.isEmpty()
                        ? messages.get(0).getOrDefault("id", "unknown") : "unknown";
                log.info("WhatsApp message sent. to={} wamid={}", maskPhone(recipient), wamid);
                return wamid;
            }
            throw new NotificationSendException("WhatsApp", "Unexpected response: " + response.getStatusCode(), true);

        } catch (HttpClientErrorException ex) {
            // 4xx = permanent (bad number, policy violation, etc.)
            log.error("WhatsApp 4xx error. to={} status={}", maskPhone(recipient), ex.getStatusCode());
            throw new NotificationSendException("WhatsApp", ex.getResponseBodyAsString(), false, ex);

        } catch (HttpServerErrorException ex) {
            // 5xx = transient, retryable
            log.error("WhatsApp 5xx error. to={} status={}", maskPhone(recipient), ex.getStatusCode());
            throw new NotificationSendException("WhatsApp", ex.getResponseBodyAsString(), true, ex);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 3);
    }
}
