package app.model.servicerequest;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceRequestStatus {
    OPEN, CLOSED;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }
}
