package app.service.storage;

import app.dto.storage.PhotoUploadDTO;
import app.recaptcha.ReCaptchaService;
import app.safesearch.GoogleImageSafeSearchService;
import io.micronaut.http.MediaType;
import io.micronaut.objectstorage.ObjectStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.util.Base64;
import java.util.UUID;

@Singleton
public class StorageService {
    private static final Logger LOG = LoggerFactory.getLogger(StorageService.class);
    private final ObjectStorageOperations<?, ?, ?> objectStorage;
    private final ReCaptchaService reCaptchaService;
    private final GoogleImageSafeSearchService googleImageClassificationService;
    private final StorageUrlUtil storageUrlUtil;

    public StorageService(ObjectStorageOperations<?, ?, ?> objectStorage, ReCaptchaService reCaptchaService, GoogleImageSafeSearchService googleImageClassificationService, StorageUrlUtil storageUrlUtil) {
        this.objectStorage = objectStorage;
        this.reCaptchaService = reCaptchaService;
        this.googleImageClassificationService = googleImageClassificationService;
        this.storageUrlUtil = storageUrlUtil;
    }

    public StorageService() {
        this.storageUrlUtil = null;
        this.objectStorage = null;
        this.reCaptchaService = null;
        this.googleImageClassificationService = null;
    }

    public String upload(@Valid PhotoUploadDTO photoUploadDTO) {
        if (photoUploadDTO.getgRecaptchaResponse() == null  || !reCaptchaService.verifyReCaptcha(photoUploadDTO.getgRecaptchaResponse())) {
            LOG.error("ReCaptcha verification failed.");
            return null;
        }

        String base64Image = photoUploadDTO.getImage();
        String dataUri = base64Image.split(",")[0];
        MediaType mediaType = MediaType.of(dataUri.substring(dataUri.indexOf(":")+1, dataUri.indexOf(";")));

        if (mediaType != MediaType.IMAGE_JPEG_TYPE && mediaType != MediaType.IMAGE_PNG_TYPE) {
            LOG.error("File must be jpeg or png.");
            return null;
        }
        String extension = mediaType.getExtension();

        String image = base64Image.split(",")[1];

        if (googleImageClassificationService.imageIsExplicit(image)) {
            LOG.error("Image does not meet SafeSearch criteria.");
            return null;
        }

        byte[] bytes = Base64.getDecoder().decode(image);
        UploadRequest request = UploadRequest.fromBytes(bytes, UUID.randomUUID()+"."+extension);
        UploadResponse<?> response = objectStorage.upload(request);

        return storageUrlUtil.getObjectUrlString(response.getKey());
    }
}
