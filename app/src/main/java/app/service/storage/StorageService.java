// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app.service.storage;

import app.dto.storage.PhotoUploadDTO;
import app.recaptcha.ReCaptchaService;
import app.safesearch.GoogleImageSafeSearchService;
import io.micronaut.http.MediaType;
import io.micronaut.objectstorage.ObjectStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Singleton
public class StorageService {
    private static final Logger LOG = LoggerFactory.getLogger(StorageService.class);
    private final ObjectStorageOperations<?, ?, ?> objectStorage;
    private final ReCaptchaService reCaptchaService;
    private final GoogleImageSafeSearchService googleImageClassificationService;
    private final StorageUrlUtil storageUrlUtil;

    private final Set<MediaType> supportedMediaTypes = Set.of(MediaType.IMAGE_PNG_TYPE,
        MediaType.IMAGE_JPEG_TYPE, MediaType.IMAGE_WEBP_TYPE);

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
        reCaptchaService.verifyReCaptcha(photoUploadDTO.getgRecaptchaResponse());

        String base64Image = photoUploadDTO.getImage();
        String dataUri = base64Image.split(",")[0];
        MediaType mediaType = MediaType.of(dataUri.substring(dataUri.indexOf(":")+1, dataUri.indexOf(";")));

        if (!supportedMediaTypes.contains(mediaType)) {
            LOG.error(String.format("File media type must be one of the following: %s",
                supportedMediaTypes));
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
