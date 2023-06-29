package app.safesearch;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.io.IOException;

@Factory
public class SafeSearchImageClientFactory {

    @Singleton
    ImageAnnotatorClient client() throws IOException {
        return ImageAnnotatorClient.create();
    }
}
