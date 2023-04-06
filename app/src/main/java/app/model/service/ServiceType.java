package app.model.service;

public enum ServiceType {
    REALTIME, BATCH, BLACKBOX;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
