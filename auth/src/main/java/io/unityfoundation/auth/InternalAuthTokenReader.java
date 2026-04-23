package io.unityfoundation.auth;

import io.micronaut.security.token.reader.HttpHeaderTokenReader;
import jakarta.inject.Singleton;

@Singleton
public class InternalAuthTokenReader extends HttpHeaderTokenReader {

    @Override
    protected String getPrefix() {
        return null;
    }

    @Override
    protected String getHeaderName() {
        return "X-Unity-Auth-Internal";
    }
}
