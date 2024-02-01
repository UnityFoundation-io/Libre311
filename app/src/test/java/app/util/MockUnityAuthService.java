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

import app.model.user.UserRepository;
import app.model.userjurisdiction.UserJurisdictionRepository;
import app.security.UnityAuthClient;
import app.security.UnityAuthService;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;

@Singleton
@Replaces(UnityAuthService.class)
public class MockUnityAuthService extends UnityAuthService {

    private String userEmail;

    public MockUnityAuthService(UnityAuthClient client, UserRepository userRepository, UserJurisdictionRepository userJurisdictionRepository) {
        super(client, userRepository, userJurisdictionRepository);
    }

    @Override
    public String extractUserEmail(String jwtSubstring) {
        return userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
