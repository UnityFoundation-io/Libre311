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

import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;

@Singleton
public class StorageUrlUtil {

    @Property(name = "app.image-storage.bucket-url-format")
    private String bucketUrlFormat;

    @Property(name = "app.image-storage.append-object-url-format")
    private String appendObjectUrlFormat;

    @Property(name = "app.image-storage.bucket")
    private String bucketId;


    public String getObjectUrlString(String blobId) {
        return String.format(getBucketUrlString().concat(appendObjectUrlFormat), blobId);
    }

    public String getBucketUrlString() {
        return String.format(bucketUrlFormat, bucketId);
    }
}
