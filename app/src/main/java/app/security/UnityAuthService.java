package app.security;

import app.model.user.User;
import app.model.user.UserRepository;
import app.model.userjurisdiction.UserJurisdiction;
import app.model.userjurisdiction.UserJurisdictionRepository;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Singleton
public class UnityAuthService {
    private static final Logger LOG = LoggerFactory.getLogger(UnityAuthService.class);

    @Property(name = "micronaut.application.name")
    protected String serviceId;

    private final UnityAuthClient client;
    private final UserRepository userRepository;
    private final UserJurisdictionRepository userJurisdictionRepository;


    public UnityAuthService(UnityAuthClient client, UserRepository userRepository, UserJurisdictionRepository userJurisdictionRepository) {
        this.client = client;
        this.userRepository = userRepository;
        this.userJurisdictionRepository = userJurisdictionRepository;
    }

    public boolean isUserPermittedForAction(String token, String jurisdictionId, Collection<String> permissions) {

        String jwtSubstring;
        if (token.startsWith("Bearer ")){
            jwtSubstring = token.substring(7);
        } else {
            LOG.error("Invalid token format.");
            return false;
        }

        String tenantId;
        try {
            JWT parse = JWTParser.parse(jwtSubstring);
            String subjectEmail = parse.getJWTClaimsSet().getSubject();
            Optional<User> userOptional = userRepository.findByEmail(subjectEmail);
            if (userOptional.isEmpty()) {
                LOG.error("Cannot find user in system.");
                return false;
            }
            User user = userOptional.get();

            Optional<UserJurisdiction> jurisdictionUserOptional =
                    userJurisdictionRepository.findByUserAndJurisdictionId(user, jurisdictionId);
            if (jurisdictionUserOptional.isEmpty()) {
                LOG.error("Cannot find user under jurisdiction.");
                return false;
            }

            tenantId = jurisdictionUserOptional.get().getJurisdiction().getTenant().getId();

        } catch (ParseException e) {
            LOG.error("Unable to parse JWT token.");
            return false;
        }

        HasPermissionRequest hasPermissionRequest = new HasPermissionRequest(
                tenantId,
                serviceId,
                new ArrayList<>(permissions));

        boolean hasPermission;
        try {
            hasPermission = client.hasPermission(hasPermissionRequest, token);
        } catch (Exception e) {
            LOG.error("Returned {}", e.getMessage());
            return false;
        }

        return hasPermission;
    }
}
