package app.security;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

import static io.micronaut.context.env.Environment.TEST;

@Client(id = "auth")
@Requires(notEnv = TEST)
public interface UnityAuthClient {
    @Post(value = "/hasPermission{?subtenant}")
    boolean hasPermission(@Body HasPermissionRequest requestDTO,
                          @Nullable @QueryValue("subtenant") Boolean subtenant,
                          @Header("Authorization") String authorizationHeader);
}
