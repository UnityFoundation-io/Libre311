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

public enum CloudStorageKind {
    CLOUD_STORAGE_KIND_GCP("Google Cloud Storage"),
    CLOUD_STORAGE_KIND_AWS("AWS S3"),
    CLOUD_STORAGE_KIND_AZURE("Azure Blob Storage"),
    CLOUD_STORAGE_KIND_LOCAL("Local storage"),
    CLOUD_STORAGE_KIND_UNKNOWN("Unknown cloud provider");

    private final String description;

    CloudStorageKind(String description) {
        this.description = description;
    }

    String getDescription() {
        return description;
    }
}
