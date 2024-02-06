package app.security;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

import static io.micronaut.context.env.Environment.TEST;

@Client(id = "auth", path = "api/")
@Requires(notEnv = TEST)
public interface UnityAuthClient {
    @Post( "/hasPermission")
    HttpResponse<HasPermissionResponse> hasPermission(@Body HasPermissionRequest requestDTO,
                               @Header("Authorization") String authorizationHeader);
}
