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

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.objectstorage.local.LocalStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import jakarta.inject.Singleton;

@Singleton
@Primary
@Requires(beans = {LocalStorageOperations.class})
public class LocalStorageUploader implements CloudStorageUploader {

    private final LocalStorageOperations storageOperations;

    public LocalStorageUploader(LocalStorageOperations storageOperations) {
        this.storageOperations = storageOperations;
    }

    @Override
    public UploadResponse<?> upload(UploadRequest request) {
        return storageOperations.upload(request);
    }

    @Override
    public String getPublicURL(UploadResponse<?> response) {
        return "http://localhost:7000/images/" + response.getKey();
    }
}
