package app.service.email;

import io.micronaut.email.Contact;
import io.micronaut.email.Email;
import io.micronaut.email.EmailSender;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface EmailService {
    void send(Email.Builder email);
}
