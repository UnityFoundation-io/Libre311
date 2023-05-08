package app.util;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;


@Replaces(AuthenticationFetcher.class)
@Singleton
public class MockAuthenticationFetcher implements AuthenticationFetcher {


    private Authentication authentication;

    @Override
    public Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        return Publishers.just(this.authentication);
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }
}