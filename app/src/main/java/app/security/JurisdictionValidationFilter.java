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
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Filter(value = {"/api/services/**", "/api/requests/**", "/api/jurisdiction-admin/**"})
public class JurisdictionValidationFilter implements HttpServerFilter {

    private static final Map<String, String> BAD_REQUEST_ERROR = Map.of(
            "code", "400",
            "description", "Request must include jurisdiction_id request parameter"
    );

    private static final Map<String, String> NOT_FOUND_ERROR = Map.of(
            "code", "404",
            "description", "Jurisdiction not found"
    );

    private final JurisdictionRepository jurisdictionRepository;

    public JurisdictionValidationFilter(JurisdictionRepository jurisdictionRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return (Publisher<MutableHttpResponse<?>>) request.getParameters().getFirst("jurisdiction_id")
                .map(jurisdictionId -> jurisdictionId.trim().isEmpty()
                        ? Publishers.just(HttpResponse.badRequest(List.of(BAD_REQUEST_ERROR)))
                        : verifyJurisdiction(jurisdictionId, chain, request))
                .orElse(Publishers.just(HttpResponse.badRequest(List.of(BAD_REQUEST_ERROR))));
    }

    private Publisher<MutableHttpResponse<?>> verifyJurisdiction(String jurisdictionId, ServerFilterChain chain, HttpRequest<?> request) {
        return Mono.fromCallable(() -> jurisdictionRepository.existsById(jurisdictionId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> !exists
                        ? Mono.just(HttpResponse.notFound(List.of(NOT_FOUND_ERROR)))
                        : Mono.from(chain.proceed(request)));
    }
}
