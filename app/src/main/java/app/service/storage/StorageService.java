package app.service.storage;

import com.google.cloud.storage.Blob;
import io.micronaut.objectstorage.ObjectStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.UUID;

@Singleton
public class StorageService {
    private static final Logger LOG = LoggerFactory.getLogger(StorageService.class);
    private final ObjectStorageOperations<?, ?, ?> objectStorage;


    public StorageService(ObjectStorageOperations<?, ?, ?> objectStorage) {
        this.objectStorage = objectStorage;
    }

    public String upload(String base64Image) {
        byte[] bytes = Base64.getDecoder().decode(base64Image);
        UploadRequest request = UploadRequest.fromBytes(bytes, UUID.randomUUID().toString());
        UploadResponse<?> response = objectStorage.upload(request);
        Blob blob = (Blob) response.getNativeResponse();
        return blob.getSelfLink();
    }
}
