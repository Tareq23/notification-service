package com.tareq23.notificationservice.domain.dispatch.port;


public class NotificationSendException extends RuntimeException {
    private final String providerName;
    private final boolean retryable;

    public NotificationSendException(String providerName, String message, boolean retryable) {
        super("[" + providerName + "] " + message);
        this.providerName = providerName;
        this.retryable = retryable;
    }

    public NotificationSendException(String providerName, String message, boolean retryable, Throwable cause) {
        super("[" + providerName + "] " + message, cause);
        this.providerName = providerName;
        this.retryable = retryable;
    }

    public String providerName() { return providerName; }

    public boolean isRetryable() { return retryable; }
}
