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

package app.util;

import app.dto.storage.PhotoUploadDTO;
import app.service.storage.StorageService;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.MediaType;
import io.micronaut.objectstorage.request.UploadRequest;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;

import java.util.Base64;
import java.util.UUID;

@Singleton
@Replaces(StorageService.class)
public class MockStorageService extends StorageService {

    public MockStorageService() {
        super();
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
