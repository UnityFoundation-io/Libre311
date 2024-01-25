package app.security;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

import static io.micronaut.context.env.Environment.TEST;

@Client(id = "auth")
@Requires(notEnv = TEST)
public interface UnityAuthClient {
    @Post(value = "/hasPermission")
    boolean hasPermission(@Body HasPermissionRequest requestDTO,
                          @Header("Authorization") String authorizationHeader);
}
