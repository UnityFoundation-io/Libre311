package app.security;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.user.User;
import app.model.user.UserRepository;
import app.model.userjurisdiction.UserJurisdiction;
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

    @Property(name = "app.service-id")
    protected Long serviceId;

    private final UnityAuthClient client;
    private final UserRepository userRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final UserJurisdictionRepository userJurisdictionRepository;


    public UnityAuthService(UnityAuthClient client, UserRepository userRepository,
                            JurisdictionRepository jurisdictionRepository,
                            UserJurisdictionRepository userJurisdictionRepository) {
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

        Long tenantId = optionalJurisdiction.get().getTenantId();
        HasPermissionRequest hasPermissionRequest = new HasPermissionRequest(tenantId, serviceId, permissions);

        boolean hasPermission;
        try {
            HttpResponse<HasPermissionResponse> hasPermissionResponseHttpResponse = client.hasPermission(hasPermissionRequest, token);
            Optional<HasPermissionResponse> body = hasPermissionResponseHttpResponse.getBody(HasPermissionResponse.class);
            if (body.isEmpty()) {
                return false;
            }

            hasPermission = body.get().isHasPermission() &&
                    validateUserExistenceAndPermissions(body.get().getUserEmail(), body.get().getPermissions(), jurisdictionId);
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

    private boolean validateUserExistenceAndPermissions(String userEmail, List<String> permissions, String jurisdictionId) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        Optional<UserJurisdiction> jurisdictionUserOptional = userJurisdictionRepository.findByUserAndJurisdictionId(user, jurisdictionId);
        return jurisdictionUserOptional.isPresent() && validatePermissions(permissions, jurisdictionUserOptional.get());
    }

    private boolean validatePermissions(List<String> permissions, UserJurisdiction userJurisdiction) {
        if (permissions != null && permissions.stream().allMatch(s -> s.contains("_ADMIN_") && s.endsWith("-SUBTENANT"))) {
            return userJurisdiction.isUserAdmin();
        }
        return true;
    }
}
