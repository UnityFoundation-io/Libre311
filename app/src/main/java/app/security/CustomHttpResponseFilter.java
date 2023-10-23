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

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

import java.util.Map;

@Filter("/**")
public class CustomHttpResponseFilter implements HttpServerFilter {
    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {

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
