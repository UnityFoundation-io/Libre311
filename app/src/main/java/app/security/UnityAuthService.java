package app.security;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.user.User;
import app.model.user.UserRepository;
import app.model.userjurisdiction.UserJurisdictionRepository;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Singleton
public class UnityAuthService {
    private static final Logger LOG = LoggerFactory.getLogger(UnityAuthService.class);

    @Property(name = "micronaut.application.name")
    protected String serviceId;

    private final UnityAuthClient client;
    private final UserRepository userRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final UserJurisdictionRepository userJurisdictionRepository;


    public UnityAuthService(UnityAuthClient client, UserRepository userRepository, JurisdictionRepository jurisdictionRepository, UserJurisdictionRepository userJurisdictionRepository) {
        this.client = client;
        this.userRepository = userRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.userJurisdictionRepository = userJurisdictionRepository;
    }

    public boolean isUserPermittedForAction(String token, String jurisdictionId, List<String> permissions) {

        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findById(jurisdictionId);
        if (optionalJurisdiction.isEmpty()) {
            return false;
        }

        String tenantId = optionalJurisdiction.get().getTenantId();
        HasPermissionRequest hasPermissionRequest = new HasPermissionRequest(tenantId, serviceId, permissions);

        boolean hasPermission;
        try {
            HttpResponse<HasPermissionResponse> hasPermissionResponseHttpResponse = client.hasPermission(hasPermissionRequest, token);
            Optional<HasPermissionResponse> body = hasPermissionResponseHttpResponse.getBody(HasPermissionResponse.class);
            if (body.isEmpty()) {
                return false;
            }
            hasPermission = body.get().isHasPermission() && existsInLocalUserJurisdictionTable(body.get().getUserEmail(), jurisdictionId);
        } catch (HttpClientResponseException e) {
            Optional<HasPermissionResponse> body = e.getResponse().getBody(HasPermissionResponse.class);
            if (body.isEmpty()) {
                LOG.error("Returned {}", e.getMessage());
            } else {
                LOG.error("Returned {}", (body.get().getErrorMessage() == null ? e.getMessage() : e.getStatus()));
            }

            return false;
        }

        return hasPermission;
    }

    private boolean existsInLocalUserJurisdictionTable(String userEmail, String jurisdictionId) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        return userJurisdictionRepository.findByUserAndJurisdictionId(user, jurisdictionId).isPresent();
    }
}
