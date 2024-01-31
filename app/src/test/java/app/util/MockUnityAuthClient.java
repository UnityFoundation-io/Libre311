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

import app.security.HasPermissionRequest;
import app.security.HasPermissionResponse;
import app.security.UnityAuthClient;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

@Singleton
public class MockUnityAuthClient implements UnityAuthClient {

    private HttpResponse<HasPermissionResponse> response;

    @Override
    public HttpResponse<HasPermissionResponse> hasPermission(HasPermissionRequest requestDTO, String authorizationHeader) {
        return response;
    }

    public HttpResponse<HasPermissionResponse> getResponse() {
        return response;
    }

    public void setResponse(HttpResponse<HasPermissionResponse> response) {
        this.response = response;
    }
}
