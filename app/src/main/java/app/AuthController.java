package app;

import app.dto.auth.GenerateTokenRequest;
import app.dto.auth.GenerateTokenResponse;
import app.dto.auth.ResetPasswordRequest;
import app.model.jurisdiction.JurisdictionRepository;
import app.security.UnityAuthService;
import app.service.email.EmailService;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.email.Email;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

@Controller("/api")
@Secured(SecurityRule.IS_ANONYMOUS)
@ExecuteOn(TaskExecutors.BLOCKING)
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final UnityAuthService authService;
    private final EmailService emailService;
    private final JurisdictionRepository jurisdictionRepository;

    @Value("${unity.auth.internal-token}")
    protected String internalToken;

    public AuthController(UnityAuthService authService, EmailService emailService, JurisdictionRepository jurisdictionRepository) {
        this.authService = authService;
        this.emailService = emailService;
        this.jurisdictionRepository = jurisdictionRepository;
    }

    @Post("/forgot-password")
    public HttpResponse<?> forgotPassword(@Body @Valid GenerateTokenRequest request, HttpRequest<?> httpRequest) {
        String baseUrl = resolveBaseUrl(httpRequest);
        try {
            HttpResponse<GenerateTokenResponse> response = authService.generateToken(request, internalToken);
            if (response.getBody().isPresent()) {
                String token = response.getBody().get().token();
                sendResetEmail(request.email(), token, baseUrl);
            }
        } catch (Exception e) {
            // We log but don't expose if the user exists or not
            LOG.error("Error generating reset token for {}: {}", request.email(), e.getMessage());
        }

        // Always return 204 for security
        return HttpResponse.noContent();
    }

    @Post("/reset-password")
    public HttpResponse<?> resetPassword(@Body @Valid ResetPasswordRequest request) {
        return authService.resetPassword(request, internalToken);
    }

    private String resolveBaseUrl(HttpRequest<?> httpRequest) {
        String referer = httpRequest.getHeaders().get("Referer");
        if (referer == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Missing Referer header");
        }
        try {
            URI uri = new URI(referer);
            String host = uri.getHost();
            if (host == null || jurisdictionRepository.findByRemoteHostsNameEquals(host).isEmpty()) {
                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Referer host is not a registered jurisdiction");
            }
            return uri.getScheme() + "://" + uri.getAuthority();
        } catch (URISyntaxException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid Referer header");
        }
    }

    private void sendResetEmail(String email, String token, String baseUrl) {
        String resetLink = String.format("%s/reset-password?token=%s", baseUrl, token);
        
        emailService.send(Email.builder()
                .to(email)
                .subject("Libre311 Password Reset")
                .body("You (or someone pretending to be you) requested a password reset. Please click the link below to set a new password:\n\n" +
                        resetLink + "\n\n" +
                        "This link will expire in 1 hour."));
    }
}
