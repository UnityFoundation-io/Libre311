package app.security;

import static app.security.SecurityRuleUtil.ALLOWED;
import static app.security.SecurityRuleUtil.REJECTED;
import static app.security.SecurityRuleUtil.UNKNOWN;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.rules.SecurityRuleResult;
import io.micronaut.web.router.MethodBasedRouteMatch;
import io.micronaut.web.router.RouteMatch;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;

@Singleton
public class RequiresPermissionsAnnotationRule implements SecurityRule {

  private final UnityAuthService unityAuthService;

  public RequiresPermissionsAnnotationRule(UnityAuthService unityAuthService) {
    this.unityAuthService = unityAuthService;
  }

  @Override
  public Publisher<SecurityRuleResult> check(HttpRequest<?> request,
      @Nullable RouteMatch<?> routeMatch, @Nullable Authentication authentication) {
    if (!(routeMatch instanceof MethodBasedRouteMatch)) {
      return UNKNOWN;
    }
    MethodBasedRouteMatch<?, ?> methodRoute = ((MethodBasedRouteMatch) routeMatch);
    if (!methodRoute.hasAnnotation(RequiresPermissions.class)) {
      return UNKNOWN;
    }

    List<Permission> declaredPermissions = resolvePermissionsFromAnnotation(methodRoute);

    Optional<String> maybeAuthorization = request.getHeaders().getAuthorization();
    if (authentication == null || maybeAuthorization.isEmpty()) {
      return REJECTED;
    }

    String bearerToken = maybeAuthorization.get().substring(7); // remove "Bearer: " prefix
    List<String> declaredPermissionsAsStrings = declaredPermissions.stream()
        .map(Enum::toString).collect(Collectors.toList());

    if (unityAuthService.isUserPermittedForAction(bearerToken, resolveJurisdictionId(request),
        declaredPermissionsAsStrings)) {
      return ALLOWED;
    }

    return REJECTED;
  }

  private List<Permission> resolvePermissionsFromAnnotation(
      MethodBasedRouteMatch<?, ?> methodRoute) {
    Optional<Permission[]> optionalValue = methodRoute.getValue(
        RequiresPermissions.class,
        Permission[].class);

    if (optionalValue.isEmpty()) {
      throw new IllegalArgumentException(
          "Permissions must be defined when using @RequiresPermissions");
    }
    return Arrays.asList(optionalValue.get());
  }

  private String resolveJurisdictionId(HttpRequest<?> request) {
    String jurisdictionId = request.getParameters().get("jurisdiction_id");
    if (jurisdictionId == null) {
      throw new IllegalArgumentException(
          "The Jurisdiction Id must exists as a query parameter when using @RequiresPermissions");
    }
    return jurisdictionId;
  }
}
