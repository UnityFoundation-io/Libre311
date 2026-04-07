package app.dto.auth;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record GenerateTokenRequest(@NotBlank String email) {
}
