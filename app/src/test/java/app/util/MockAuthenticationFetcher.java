package app.util;
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
// package app.util;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import jakarta.inject.Singleton;
import java.util.Map;
import org.reactivestreams.Publisher;


@Replaces(AuthenticationFetcher.class)
@Singleton
public class MockAuthenticationFetcher implements AuthenticationFetcher<HttpRequest<?>> {

    static class MockAuthentication implements Authentication {

        @Override
        public Map<String, Object> getAttributes() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }

    public static final Authentication DEFAULT_MOCK_AUTHENTICATION = new MockAuthentication();


    private Authentication authentication;

    @Override
    public Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        return Publishers.just(this.authentication);
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }
}