package app;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.jackson.nullable.JsonNullableModule;

@Factory
public class JacksonFactory {
    @Singleton
    ObjectMapper objectMapper(ObjectMapper mapper) {
        mapper.registerModule(new JsonNullableModule());
        return mapper;
    }
}
