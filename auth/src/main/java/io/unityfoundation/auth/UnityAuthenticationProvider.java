package io.unityfoundation.auth;

import static io.micronaut.security.authentication.AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.ReactiveAuthenticationProvider;
import io.unityfoundation.auth.entities.User;
import io.unityfoundation.auth.entities.UserRepo;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Singleton
public class UnityAuthenticationProvider implements ReactiveAuthenticationProvider<HttpRequest<?>,Object,Object> {

  private final UserRepo userRepo;
  private final PasswordEncoder passwordEncoder;

  public UnityAuthenticationProvider(UserRepo userRepo,
      PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }


  private AuthenticationFailed validate(User user,
      AuthenticationRequest<?, ?> authenticationRequest) {
    AuthenticationFailed authenticationFailed = null;
    if (user == null) {
      authenticationFailed = new AuthenticationFailed(CREDENTIALS_DO_NOT_MATCH);
    } else if (!passwordEncoder.matches(authenticationRequest.getSecret().toString(),
        user.getPassword())) {
      authenticationFailed = new AuthenticationFailed(CREDENTIALS_DO_NOT_MATCH);
    }

    return authenticationFailed;
  }

  private User findUser(AuthenticationRequest<?, ?> authRequest) {
    final Object username = authRequest.getIdentity();
    return userRepo.findUserForAuthentication(username.toString()).orElse(null);
  }

  @Override
  public @NonNull Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> requestContext,
      @NonNull AuthenticationRequest<Object, Object> authenticationRequest) {
      return authenticate(authenticationRequest);
  }

  @Override
  public @NonNull Publisher<AuthenticationResponse> authenticate(
      @NonNull AuthenticationRequest<Object, Object> authenticationRequest) {
            return Mono.fromCallable(() -> findUser(authenticationRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(user -> {
          AuthenticationFailed authenticationFailed = validate(user, authenticationRequest);
          if (authenticationFailed != null) {
            return Mono.just(AuthenticationResponse.failure(authenticationFailed.getReason().toString()));
          } else {
            return Mono.just(AuthenticationResponse.success(
                    (String) authenticationRequest.getIdentity(),
                    List.of("USER"),
                    Map.of(
                            "first_name", Objects.toString(user.getFirstName(), ""),
                            "last_name", Objects.toString(user.getLastName(), "")
                    )
            ));
          }
        });
  }
}

