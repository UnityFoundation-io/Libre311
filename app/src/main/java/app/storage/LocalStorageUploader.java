package app.storage;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.objectstorage.local.LocalStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Singleton;

@Singleton
@Primary
@Requires(beans = {LocalStorageOperations.class})
public class LocalStorageUploader implements CloudStorageUploader {

    private final LocalStorageOperations storageOperations;

    public LocalStorageUploader(LocalStorageOperations storageOperations) {
        this.storageOperations = storageOperations;
    }

    @Override
    public UploadResponse<?> upload(UploadRequest request) {
        return storageOperations.upload(request);
    }

    @Override
    public String getPublicURL(UploadResponse<?> response) {
        return "http://localhost:7000/images/" + response.getKey();
    }
}
