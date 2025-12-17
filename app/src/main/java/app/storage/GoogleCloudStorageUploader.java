package app.storage;

import com.google.cloud.storage.Blob;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Singleton;

@Singleton
@Requires(property = "micronaut.object-storage.gcp")
public class GoogleCloudStorageUploader implements CloudStorageUploader {

    GoogleCloudStorageOperations objectStorage;

    GoogleCloudStorageUploader(GoogleCloudStorageOperations objectStorage) {
        this.objectStorage = objectStorage;
    }

    @Override
    public UploadResponse<Blob> upload(@NonNull UploadRequest request) {
        return objectStorage.upload(request);
    }

    @Override
    public String getPublicURL(UploadResponse<?> response) {
        Object nativeResponse = response.getNativeResponse();
        if (!(nativeResponse instanceof Blob res)) {
            throw new UnexpectedUploadResponseException(CloudStorageKind.CLOUD_STORAGE_KIND_GCP,
                    getCloudStorageKind(nativeResponse));
        }

        return UriBuilder.of("https://storage.googleapis.com").path(res.getBucket())
            .path(res.getName()).build().toString();
    }
}
