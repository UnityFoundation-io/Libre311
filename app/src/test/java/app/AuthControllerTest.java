package app;

import app.dto.auth.GenerateTokenRequest;
import app.dto.auth.GenerateTokenResponse;
import app.dto.auth.ResetPasswordRequest;
import app.security.UnityAuthService;
import app.service.email.EmailService;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.email.Email;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MicronautTest
@Property(name = "app.base-url", value = "http://localhost:3000")
@Property(name = "unity.auth.internal-token", value = "test-secret")
public class AuthControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    UnityAuthService authService;

    @Inject
    EmailService emailService;

    @MockBean(UnityAuthService.class)
    UnityAuthService authService() {
        return mock(UnityAuthService.class);
    }

    @MockBean(EmailService.class)
    EmailService emailService() {
        return mock(EmailService.class);
    }

    @Test
    void testForgotPassword() {
        reset(authService, emailService);
        when(authService.generateToken(any(), any()))
                .thenReturn(HttpResponse.ok(new GenerateTokenResponse("test-token")));

        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.POST("/api/forgot-password", new GenerateTokenRequest("test@example.com"))
                        .header("Referer", "http://my-ui.test/login")
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
        verify(authService).generateToken(any(), any());
        verify(emailService).send(any());
    }

    @Test
    void testResetPassword() {
        reset(authService, emailService);
        when(authService.resetPassword(any()))
                .thenReturn(HttpResponse.ok());

        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.POST("/api/reset-password", new ResetPasswordRequest("test-token", "new-pass"))
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        verify(authService).resetPassword(any());
    }
}
