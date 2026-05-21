package com.tareq23.notificationservice.domain.shared;

import java.util.Objects;
import java.util.UUID;


public final class UserId {


    private final String value;

    public UserId(String value) {
        Objects.requireNonNull(value, "UserId must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("UserId must not be blank");
        this.value = value;
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
