package app;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.security.UnityAuthClient;
import app.security.UnityAuthUserPermissionsRequest;
import app.security.UserPermissionsResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/api")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class PermissionsController {


    @Property(name = "app.service-id")
    protected Long serviceId;

    UnityAuthClient unityAuthClient;
    JurisdictionRepository jurisdictionRepository;

    public PermissionsController(UnityAuthClient unityAuthClient,
        JurisdictionRepository jurisdictionRepository) {
        this.unityAuthClient = unityAuthClient;
        this.jurisdictionRepository = jurisdictionRepository;
    }

    @Introspected
    public static class LibreUserPermissionsRequest {

        String jurisdictionId;

        public String getJurisdictionId() {
            return jurisdictionId;
        }

        public void setJurisdictionId(String jurisdictionId) {
            this.jurisdictionId = jurisdictionId;
        }
    }

    @Post("/principal/permissions")
    public HttpResponse<UserPermissionsResponse> getUserPermissions(
        @Body LibreUserPermissionsRequest reqBody, HttpRequest<?> request) {
        Jurisdiction jurisdiction = jurisdictionRepository.findByJurisdictionId(
            reqBody.getJurisdictionId());
        // later on we will augment the permissions returned by unityAuthClient with any Libre defined permissions.  Currently, there are none so we simply proxy the call to unity auth service.
        return this.unityAuthClient.getUserPermissions(
            new UnityAuthUserPermissionsRequest(jurisdiction.getTenantId(), serviceId),
            request.getHeaders().getAuthorization().orElseThrow(
                () -> new HttpStatusException(HttpStatus.FORBIDDEN, "User must be authenticated")));
    }
}
