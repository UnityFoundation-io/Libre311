package app.service.storage;

import com.google.cloud.storage.Blob;
import io.micronaut.http.MediaType;
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
        String dataUri = base64Image.split(",")[0];
        MediaType mediaType = MediaType.of(dataUri.substring(dataUri.indexOf(":")+1, dataUri.indexOf(";")));

        if (mediaType != MediaType.IMAGE_JPEG_TYPE && mediaType != MediaType.IMAGE_PNG_TYPE) {
            return null;
        }
        String extension = mediaType.getExtension();

        String image = base64Image.split(",")[1];
        byte[] bytes = Base64.getDecoder().decode(image);
        UploadRequest request = UploadRequest.fromBytes(bytes, UUID.randomUUID()+"."+extension);
        UploadResponse<?> response = objectStorage.upload(request);
        Blob blob = (Blob) response.getNativeResponse();
        String imageLink = "https://storage.cloud.google.com/" + blob.getBucket() + "/" + blob.getName();

        return imageLink;
    }
}
