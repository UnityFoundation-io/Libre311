package app.model.service;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceType {
    REALTIME, BATCH, BLACKBOX;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }
}
