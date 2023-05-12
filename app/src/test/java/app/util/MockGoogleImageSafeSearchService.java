package app.util;

import app.recaptcha.ReCaptchaService;
import app.safesearch.GoogleImageSafeSearchService;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;

@Singleton
@Replaces(GoogleImageSafeSearchService.class)
public class MockGoogleImageSafeSearchService extends GoogleImageSafeSearchService {

    public MockGoogleImageSafeSearchService() {}

    public boolean imageIsExplicit(String image) {
        return false;
    }
}
