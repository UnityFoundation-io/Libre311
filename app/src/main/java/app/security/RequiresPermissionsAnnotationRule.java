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
        if (maybeAuthorization.isEmpty()) {
            return REJECTED;
        }

        String bearerToken = maybeAuthorization.get();

        String jurisdictionId = request.getParameters().get("jurisdiction_id");
        String tenantId = request.getParameters().get("tenant_id");
        if (jurisdictionId == null && tenantId == null) {
            throw new IllegalArgumentException(
                    "Either the Jurisdiction Id or Tenant Id must exist as a query parameter when using @RequiresPermissions");
        }

        boolean result;
        if (jurisdictionId != null) {
            result = unityAuthService.isUserPermittedForJurisdictionAction(bearerToken, jurisdictionId, declaredPermissions);
        } else {
            result = unityAuthService.isUserPermittedForTenantAction(bearerToken, Long.valueOf(tenantId), declaredPermissions);
        }

        if (result) {
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
}
