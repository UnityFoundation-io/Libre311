package app.service.storage;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class StorageUrlUtil {

    @Property(name = "app.image-storage.bucket-url-format")
    private String bucketUrlFormat;

    @Property(name = "app.image-storage.append-object-url-format")
    private String appendObjectUrlFormat;

    @Value("${app.image-storage.bucket}")
    private String bucketId;


    public String getObjectUrlString(String blobId) {
        return String.format(getBucketUrlString().concat(appendObjectUrlFormat), blobId);
    }

    public String getBucketUrlString() {
        return String.format(bucketUrlFormat, bucketId);
    }
}
