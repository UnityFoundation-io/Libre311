package app.imagedetection;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.objectstorage.local.LocalStorageOperations;
import jakarta.inject.Singleton;

@Singleton
@Primary
@Requires(beans = {LocalStorageOperations.class})
public class LocalImageDetector implements ImageDetector {
    @Override
    public void preventExplicitImage(byte[] bytes) {
        // don't bother
    }
}
