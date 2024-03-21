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

package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.util.MockReCaptchaService;
import app.util.MockStorageService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

@MicronautTest
public class ImageStorageControllerTest {

    @Inject
    @Client("/api")
    HttpClient client;

    @Inject
    MockReCaptchaService mockReCaptchaService;

    @Inject
    MockStorageService mockStorageService;

    // upload
    @Test
    public void canUpload() throws IOException {
        File tmpFile = File.createTempFile("data", ".png");
        MultipartBody requestBody = MultipartBody.builder()
            .addPart("file", tmpFile.getName(), MediaType.IMAGE_PNG_TYPE, tmpFile)
            .addPart("g_recaptcha_response", "string")
            .build();
        HttpRequest<?> request = HttpRequest.POST("/image", requestBody)
            .contentType(MediaType.MULTIPART_FORM_DATA_TYPE);

        HttpResponse<String> response = client.toBlocking().exchange(request, String.class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<String> body = response.getBody(String.class);
        assertTrue(body.isPresent());
        assertEquals("https://storage.googleapis.com/test-bucket/filename.jpg", body.get());
    }
}
