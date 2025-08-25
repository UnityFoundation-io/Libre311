package app.service.storage;

import app.exception.Libre311BaseException;
import com.google.cloud.storage.Blob;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpStatus;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;

public interface CloudStorageHelper {

    static class UnexpectedUploadResponseException extends Libre311BaseException {

        UnexpectedUploadResponseException(CloudStorageKind expectedKind, CloudStorageKind actualKind) {
            super(String.format("Expect UploadResponse from '%s'. Receive from '%s'",
                    expectedKind.getDescription(), actualKind.getDescription()), HttpStatus.BAD_REQUEST);
        }
    }

    UploadResponse<?> upload(@NonNull UploadRequest request);

    String getPublicURL(UploadResponse<?> response);

    default CloudStorageKind getCloudStorageKind(Object nativeResponse) {
        CloudStorageKind actualKind = CloudStorageKind.CLOUD_STORAGE_KIND_UNKNOWN;
        if (nativeResponse instanceof Blob) {
            actualKind = CloudStorageKind.CLOUD_STORAGE_KIND_GCP;
        }
        // TODO(sonndinh): Add check for AWS S3 and Azure Blob Storage
        return actualKind;
    }
}
