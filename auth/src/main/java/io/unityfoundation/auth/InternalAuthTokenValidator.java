package io.unityfoundation.auth;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.validator.TokenValidator;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Singleton
public class InternalAuthTokenValidator implements TokenValidator<HttpRequest<?>> {

    @Value("${unity.auth.internal-token}")
    private String internalToken;

    @Override
    public Publisher<Authentication> validateToken(String token, HttpRequest<?> request) {
        if (internalToken != null && MessageDigest.isEqual(
                internalToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8))) {
            return Mono.just(Authentication.build("internal-service", List.of("INTERNAL_SERVICE")));
        }
        return Mono.empty();
    }
}
