package app.model.servicerequest;

public enum ServiceRequestStatus {
    OPEN, CLOSED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
