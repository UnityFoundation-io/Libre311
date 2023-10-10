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
