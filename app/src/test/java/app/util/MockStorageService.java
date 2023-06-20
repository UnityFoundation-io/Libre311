package app.util;

import app.dto.storage.PhotoUploadDTO;
import app.service.storage.StorageService;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.env.Environment;
import io.micronaut.http.MediaType;
import io.micronaut.objectstorage.request.UploadRequest;
import jakarta.inject.Singleton;

import javax.validation.Valid;
import java.util.Base64;
import java.util.UUID;

@Singleton
@Replaces(StorageService.class)
public class MockStorageService extends StorageService {

    public MockStorageService(Environment environment) {
        super(environment);
    }

    public String upload(@Valid PhotoUploadDTO photoUploadDTO) {
        String base64Image = photoUploadDTO.getImage();
        String dataUri = base64Image.split(",")[0];
        MediaType mediaType = MediaType.of(dataUri.substring(dataUri.indexOf(":")+1, dataUri.indexOf(";")));

        if (mediaType != MediaType.IMAGE_JPEG_TYPE && mediaType != MediaType.IMAGE_PNG_TYPE) {
            return null;
        }
        String extension = mediaType.getExtension();

        String image = base64Image.split(",")[1];
        byte[] bytes = Base64.getDecoder().decode(image);
        UploadRequest request = UploadRequest.fromBytes(bytes, UUID.randomUUID()+"."+extension);

        return "https://storage.googleapis.com/test-bucket/filename.jpg";
    }
}
