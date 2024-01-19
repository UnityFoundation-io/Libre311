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

package app.security;

import app.model.jurisdiction.JurisdictionRepository;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Map;

@Filter(value = {"/api/services/**", "/api/requests/**"})
public class CustomHttpResponseFilter implements HttpServerFilter {

    private final JurisdictionRepository jurisdictionRepository;

    public CustomHttpResponseFilter(JurisdictionRepository jurisdictionRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {

        String jurisdictionId = request.getParameters().get("jurisdiction_id");

        if(jurisdictionId == null || jurisdictionId.trim().isEmpty()) {
            return Publishers.just(HttpResponse.badRequest(List.of(Map.of(
                    "code", "400", "description",
                    "Request must include jurisdiction_id request parameter")
            )));
        }

        // todo: Address 'Possible call in non-blocking context'
        if (!jurisdictionRepository.existsById(jurisdictionId)) {
            return Publishers.just(HttpResponse.notFound(List.of(Map.of(
                    "code", "404",
                    "description", "jurisdiction_id not found."
            ))));
        }

        // see https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP
        return Publishers.map(chain.proceed(request), mutableHttpResponse -> mutableHttpResponse.headers(Map.of(
                // Please see svelte.config.js for CSP configuration
//                "Content-Security-Policy", "default-src 'self'",
//                "Content-Security-Policy-Report-Only", "default-src 'self'",

                "Strict-Transport-Security", "max-age=31536000; includeSubDomains",
                "Permissions-Policy", "geolocation=(*), magnetometer=(*), " +
                        "camera=(self), display-capture=(self), fullscreen=(self), " +
                        "payment=(), microphone=()",
                "X-Frame-Options", "DENY",
                "X-Content-Type-Options", "nosniff",
                HttpHeaders.REFERRER_POLICY, "no-referrer"
        )));
    }
}
