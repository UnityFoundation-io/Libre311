package app;

import app.dto.auth.GenerateTokenRequest;
import app.dto.auth.GenerateTokenResponse;
import app.dto.auth.ResetPasswordRequest;
import app.security.UnityAuthService;
import app.service.email.EmailService;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
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

    @Value("${unity.auth.internal-token}")
    protected String internalToken;

    public AuthController(UnityAuthService authService, EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
    }

    @Post("/forgot-password")
    public HttpResponse<?> forgotPassword(@Body @Valid GenerateTokenRequest request, HttpRequest<?> httpRequest) {
        try {
            HttpResponse<GenerateTokenResponse> response = authService.generateToken(request, internalToken);
            if (response.getBody().isPresent()) {
                String token = response.getBody().get().token();
                sendResetEmail(request.email(), token, httpRequest);
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

    private void sendResetEmail(String email, String token, HttpRequest<?> httpRequest) {
        String origin = httpRequest.getHeaders().get("Referer");
        String baseUrl = "";

        if (origin != null) {
            try {
                URI uri = new URI(origin);
                baseUrl = uri.getScheme() + "://" + uri.getAuthority();
            } catch (URISyntaxException e) {
                LOG.warn("Could not parse Referer header: {}", origin);
            }
        }

        String resetLink = String.format("%s/reset-password?token=%s", baseUrl, token);
        
        emailService.send(Email.builder()
                .to(email)
                .subject("Libre311 Password Reset")
                .body("You (or someone pretending to be you) requested a password reset. Please click the link below to set a new password:\n\n" +
                        resetLink + "\n\n" +
                        "This link will expire in 1 hour."));
    }
}
