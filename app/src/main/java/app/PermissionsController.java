package app;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.security.UnityAuthClient;
import app.security.UnityAuthService;
import app.security.UnityAuthUserPermissionsRequest;
import app.security.UserPermissionsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/api")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class PermissionsController {

    UnityAuthService unityAuthService;

    public PermissionsController(UnityAuthService unityAuthService) {
        this.unityAuthService = unityAuthService;
    }

    @Get("/{jurisdictionId}/principal/permissions")
    public HttpResponse<UserPermissionsResponse> getUserPermissions(
        @PathVariable String jurisdictionId, HttpRequest<?> request) {

        return this.unityAuthService.getUserPermissions(jurisdictionId,
            request.getHeaders().getAuthorization().orElseThrow(
                () -> new HttpStatusException(HttpStatus.FORBIDDEN, "User must be authenticated")));
    }
}
