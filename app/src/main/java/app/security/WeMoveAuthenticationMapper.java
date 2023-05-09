package app.security;

import app.model.user.UserRepository;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.DefaultOpenIdAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdClaims;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdTokenResponse;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Optional;

@Replaces(DefaultOpenIdAuthenticationMapper.class)
@Singleton
@Named("google")
public class WeMoveAuthenticationMapper implements OpenIdAuthenticationMapper {

    private final UserRepository userRepository;

    public WeMoveAuthenticationMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthenticationResponse createAuthenticationResponse(String providerName,
                                                               OpenIdTokenResponse tokenResponse,
                                                               OpenIdClaims openIdClaims,
                                                               State state) {
        return getAuthenticationResponse(openIdClaims.getEmail());
    }

    public AuthenticationResponse getAuthenticationResponse(String userEmail) {
        return Optional.ofNullable(userEmail)
                .flatMap(userRepository::findByEmail)
                .map(user -> AuthenticationResponse.success(userEmail))
                .orElseGet(() -> AuthenticationResponse.failure(AuthenticationFailureReason.USER_NOT_FOUND));
    }

}