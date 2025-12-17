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

import app.storage.StorageService;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.multipart.CompletedFileUpload;
import jakarta.inject.Singleton;

@Singleton
@Replaces(StorageService.class)
public class MockStorageService extends StorageService {

    public MockStorageService() {
        super(null, null);
    }

    @Override
    public String upload(CompletedFileUpload file){
        return "https://storage.googleapis.com/test-bucket/filename.jpg";
    }
}
