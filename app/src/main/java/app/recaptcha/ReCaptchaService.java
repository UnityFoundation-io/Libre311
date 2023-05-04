package app.recaptcha;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class ReCaptchaService {

    @Inject
    ReCaptchaClient client;

    @Property(name = "wemove.recaptcha.secret")
    protected String secret;

    public Boolean verifyReCaptcha(String response) {
        Map map = client.verifyReCaptcha(this.secret, response);
        Boolean success = (Boolean) map.get("success");
        return success;
    }
}
