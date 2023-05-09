package app.recaptcha;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

import java.util.Map;

@Client("https://www.google.com/recaptcha/api/siteverify")
public interface ReCaptchaClient {

    @Post(produces = MediaType.APPLICATION_FORM_URLENCODED)
    Map verifyReCaptcha(String secret, String response);
}
