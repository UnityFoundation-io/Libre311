package app.recaptcha;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class ReCaptchaPayload {
    private String secret;
    private String response;

    public ReCaptchaPayload(String secret, String response) {
        this.secret = secret;
        this.response = response;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
