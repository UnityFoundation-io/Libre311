package app.service.storage;

public enum CloudStorageKind {
    CLOUD_STORAGE_KIND_GCP("Google Cloud Storage"),
    CLOUD_STORAGE_KIND_AWS("AWS S3"),
    CLOUD_STORAGE_KIND_AZURE("Azure Blob Storage"),
    CLOUD_STORAGE_KIND_LOCAL("Local storage"),
    CLOUD_STORAGE_KIND_UNKNOWN("Unknown cloud provider");

    private final String description;

    CloudStorageKind(String description) {
        this.description = description;
    }

    String getDescription() {
        return description;
    }
}
