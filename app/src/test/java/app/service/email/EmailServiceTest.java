package app.service.email;

import io.micronaut.email.Email;
import io.micronaut.email.EmailSender;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@MicronautTest
public class EmailServiceTest {

    @Inject
    EmailService emailService;

    @Inject
    EmailSender<?, ?> emailSender;

    @Test
    void testEmailServiceIsInjected() {
        assertNotNull(emailService);
    }

    @Test
    void testSendEmail() {
        assertDoesNotThrow(() -> {
            emailService.send(Email.builder()
                    .to("test@example.com")
                    .subject("Test Subject")
                    .body("Test Body")
            );
        });
        verify(emailSender).send(any(Email.Builder.class));
    }

    @MockBean(EmailSender.class)
    EmailSender<?, ?> emailSender() {
        return mock(EmailSender.class);
    }
}
