package app.service.email;

import io.micronaut.email.Email;
import io.micronaut.email.EmailSender;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EmailServiceDefault implements EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailServiceDefault.class);

    private final EmailSender<?, ?> emailSender;

    public EmailServiceDefault(EmailSender<?, ?> emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void send(Email.Builder email) {
        try {
            emailSender.send(email);
            LOG.info("Email sent successfully");
        } catch (Exception e) {
            LOG.error("Failed to send email: {}", e.getMessage(), e);
        }
    }
}
