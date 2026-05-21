package com.tareq23.notificationservice.infrastructure.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WhatsAppConfig {


    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
