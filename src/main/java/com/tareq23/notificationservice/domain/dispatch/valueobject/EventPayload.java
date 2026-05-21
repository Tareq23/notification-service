package com.tareq23.notificationservice.domain.dispatch.valueobject;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public final class EventPayload {


    private final Map<String, String> data;

    public EventPayload(Map<String, String> data) {
        Objects.requireNonNull(data, "Payload data must not be null");
        this.data = Collections.unmodifiableMap(new HashMap<>(data));
    }

    public static EventPayload empty() {
        return new EventPayload(Map.of());
    }

    public static EventPayload of(Map<String, String> data) {
        return new EventPayload(data);
    }

    public Map<String, String> data() {
        return data;
    }

    public String get(String key) {
        return data.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventPayload other)) return false;
        return data.equals(other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "EventPayload" + data;
    }

}
