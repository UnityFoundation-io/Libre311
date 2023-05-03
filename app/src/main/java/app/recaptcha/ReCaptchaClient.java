package app.recaptcha;

import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

import java.util.Map;

@Client("https://www.google.com/recaptcha/api/siteverify")
public interface ReCaptchaClient {

    @Post
    Map verifyReCaptcha(ReCaptchaPayload payload);
}
