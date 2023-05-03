package app;

import app.dto.storage.PhotoUploadDTO;
import app.service.storage.StorageService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import javax.validation.Valid;

@Controller("/api/image")
public class ImageStorageController {

    private final StorageService storageService;

    public ImageStorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Post
    @ExecuteOn(TaskExecutors.IO)
    public String upload(@Valid @Body PhotoUploadDTO photoUploadDTO) {
        System.out.println("photoUploadDTO.getImage() " + photoUploadDTO.getImage());
        System.out.println("photoUploadDTO.getgRecaptchaResponse() " + photoUploadDTO.getgRecaptchaResponse());
        return storageService.upload(photoUploadDTO);
    }
}
