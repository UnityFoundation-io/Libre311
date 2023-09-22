package app.service.storage;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class StorageUrlUtil {

    @Property(name = "wemove.image-storage.bucket-url-format")
    private String bucketUrlFormat;

    @Property(name = "wemove.image-storage.append-object-url-format")
    private String appendObjectUrlFormat;

    @Property(name = "wemove.image-storage.bucket")
    private String bucketId;


    public String getObjectUrlString(String blobId) {
        return String.format(getBucketUrlString().concat(appendObjectUrlFormat), blobId);
    }

    public String getBucketUrlString() {
        return String.format(bucketUrlFormat, bucketId);
    }
}
