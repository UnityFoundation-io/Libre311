package app.dto.auth;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record GenerateTokenResponse(@NotBlank String token) {
}
