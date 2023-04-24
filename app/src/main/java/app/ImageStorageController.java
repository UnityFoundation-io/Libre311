package app;

import app.service.storage.StorageService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@Controller("/api/image")
public class ImageStorageController {

    private final StorageService storageService;

    public ImageStorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Post(produces = MediaType.APPLICATION_JSON, consumes = MediaType.TEXT_PLAIN)
    @ExecuteOn(TaskExecutors.IO)
    public String upload(String base64Image) {
        return storageService.upload(base64Image);
    }
}
