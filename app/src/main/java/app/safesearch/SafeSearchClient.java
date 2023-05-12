package app.safesearch;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

import java.util.Map;

@Client("https://vision.googleapis.com/v1/images:annotate")
public interface SafeSearchClient {

    @Post("{?key}")
    Map classifyImage(@Body Map payload, @QueryValue String key);
}
