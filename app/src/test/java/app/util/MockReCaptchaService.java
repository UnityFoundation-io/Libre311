package app.util;

import app.recaptcha.ReCaptchaService;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;

@Singleton
@Replaces(ReCaptchaService.class)
public class MockReCaptchaService extends ReCaptchaService {

    public MockReCaptchaService() {}

    public Boolean verifyReCaptcha(String response) {
        return true;
    }
}
