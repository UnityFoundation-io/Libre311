// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//     http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app;

import app.service.storage.StorageService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller("/api/image")
@Secured(SecurityRule.IS_ANONYMOUS)
@Tag(name = "Image")
public class ImageStorageController {

    private final StorageService storageService;

    public ImageStorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Post
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ExecuteOn(TaskExecutors.IO)
    public String upload(@Part("file") CompletedFileUpload file,
        @Part("g_recaptcha_response") String gRecaptchaResponse) {
        return storageService.upload(file, gRecaptchaResponse);
    }
}
