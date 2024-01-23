package app.security;

import app.model.user.User;
import app.model.user.UserRepository;
import app.model.userjurisdiction.UserJurisdictionRepository;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

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

    public boolean isUserPermittedForAction(String token, Collection<String> permissions) {
        String jwtSubstring = token.split("bearer ")[1];
        try {
            JWT parse = JWTParser.parse(jwtSubstring);
            String subjectEmail = parse.getJWTClaimsSet().getSubject();
            Optional<User> userOptional = userRepository.findByEmail(subjectEmail);
            if (userOptional.isEmpty()) {
                LOG.warn("Cannot find user.");
                return false;
            }

            User user = userOptional.get();

            userJurisdictionRepository.findByUser(user);


        } catch (ParseException e) {
            LOG.error("Unable to parse JWT token.");
//            throw new RuntimeException(e);
            return false;
        }

        HasPermissionRequest hasPermissionRequest = new HasPermissionRequest(1L, serviceId, new ArrayList<>(permissions));

        boolean hasPermission;
        try {
            hasPermission = client.hasPermission(hasPermissionRequest, null, token);
        } catch (Exception e) {
            LOG.error("Returned {}", e.getMessage());
            return false;
        }

        return hasPermission;
    }
}
