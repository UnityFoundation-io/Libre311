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

package app.storage;

import app.recaptcha.RecaptchaRequest;
import io.micronaut.core.annotation.Introspected;

import io.micronaut.http.multipart.CompletedFileUpload;

@Introspected
public class FileUploadRequest implements RecaptchaRequest {
    private final CompletedFileUpload file;
    private final String g_recaptcha_response;

    // NO @Part annotations here.
    // Micronaut will match "file" to "file" and "g_recaptcha_response" to "g_recaptcha_response"
    public FileUploadRequest(CompletedFileUpload file, String g_recaptcha_response) {
        this.file = file;
        this.g_recaptcha_response = g_recaptcha_response;
    }

    public CompletedFileUpload getFile() {
        return file;
    }

    @Override
    public String getgRecaptchaResponse() {
        return g_recaptcha_response;
    }
}
