package com.tareq23.notificationservice.domain.dispatch.aggregate;

import java.util.Objects;



public final class IdempotencyKey {


    private final String value;

    public IdempotencyKey(String value) {
        Objects.requireNonNull(value, "IdempotencyKey must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("IdempotencyKey must not be blank");
        this.value = value;
    }

    public static IdempotencyKey of(String value) {
        return new IdempotencyKey(value);
    }


    public static IdempotencyKey from(String userId, String templateKey, String correlationId) {
        String raw = userId + "::" + templateKey + "::" + correlationId;
        return new IdempotencyKey(raw);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdempotencyKey other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "IdempotencyKey(" + value + ")";
    }

}
