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

import app.exception.Libre311BaseException;
import app.recaptcha.ReCaptchaService;
import app.imagedetection.ImageDetector;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StorageService {

    static class UnsupportedMediaTypeException extends Libre311BaseException {

        public UnsupportedMediaTypeException(MediaType mediaType) {
            super(String.format("The MediaType: %s is not supported.", mediaType),
                HttpStatus.BAD_REQUEST);
        }

        public UnsupportedMediaTypeException() {
            super("MediaType missing from file upload but is required.", HttpStatus.BAD_REQUEST);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(StorageService.class);
    private final CloudStorageUploader cloudStorageUploader;
    private final ReCaptchaService reCaptchaService;
    private final ImageDetector imageDetector;

    private final Set<MediaType> supportedMediaTypes = Set.of(MediaType.IMAGE_PNG_TYPE,
        MediaType.IMAGE_JPEG_TYPE, MediaType.IMAGE_WEBP_TYPE);

    public StorageService(CloudStorageUploader cloudStorageUploader,
                          ReCaptchaService reCaptchaService,
                          ImageDetector imageDetector) {
        this.cloudStorageUploader = cloudStorageUploader;
        this.reCaptchaService = reCaptchaService;
        this.imageDetector = imageDetector;
    }

    public String upload(CompletedFileUpload file, String gRecaptchaResponse) {
        reCaptchaService.verifyReCaptcha(gRecaptchaResponse);
        MediaType mediaType = file.getContentType().orElseThrow(UnsupportedMediaTypeException::new);
        if (!supportedMediaTypes.contains(mediaType)) {
            throw new UnsupportedMediaTypeException(mediaType);
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        imageDetector.preventExplicitImage(fileBytes);
        UploadResponse<?> response = cloudStorageUploader.upload(
            UploadRequest.fromBytes(fileBytes, createName(mediaType)));
        return cloudStorageUploader.getPublicURL(response);
    }

    private static String createName(MediaType mediaType) {
        return UUID.randomUUID() + "." + mediaType.getExtension();
    }
}
