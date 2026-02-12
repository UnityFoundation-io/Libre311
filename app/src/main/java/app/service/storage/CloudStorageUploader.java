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
import com.azure.storage.blob.models.BlockBlobItem;
import com.google.cloud.storage.Blob;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpStatus;
import io.micronaut.objectstorage.local.LocalStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public interface CloudStorageUploader {

     class UnexpectedUploadResponseException extends Libre311BaseException {

        UnexpectedUploadResponseException(CloudStorageKind expectedKind, CloudStorageKind actualKind) {
            super(String.format("Expect UploadResponse from '%s'. Receive from '%s'",
                    expectedKind.getDescription(), actualKind.getDescription()), HttpStatus.BAD_REQUEST);
        }
    }

    UploadResponse<?> upload(@NonNull UploadRequest request);

    String getPublicURL(UploadResponse<?> response);

    default CloudStorageKind getCloudStorageKind(Object nativeResponse) {
        CloudStorageKind actualKind = CloudStorageKind.CLOUD_STORAGE_KIND_UNKNOWN;
        if (nativeResponse instanceof Blob) {
            actualKind = CloudStorageKind.CLOUD_STORAGE_KIND_GCP;
        } else if (nativeResponse instanceof PutObjectResponse) {
            actualKind = CloudStorageKind.CLOUD_STORAGE_KIND_AWS;
        } else if (nativeResponse instanceof BlockBlobItem) {
            actualKind = CloudStorageKind.CLOUD_STORAGE_KIND_AZURE;
        } else if (nativeResponse instanceof LocalStorageOperations) {
            actualKind = CloudStorageKind.CLOUD_STORAGE_KIND_LOCAL;
        }
        return actualKind;
    }
}
