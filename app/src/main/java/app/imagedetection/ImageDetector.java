package app.imagedetection;

import app.exception.Libre311BaseException;
import io.micronaut.http.HttpStatus;

public interface ImageDetector {

    public static class ExplicitImageDetected extends Libre311BaseException {
        public ExplicitImageDetected() {
            super("The uploaded image has a high likelihood of being explicit and was rejected.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public void preventExplicitImage(byte[] bytes);
}
