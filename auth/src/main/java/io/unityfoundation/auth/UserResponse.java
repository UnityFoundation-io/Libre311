package io.unityfoundation.auth;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Serdeable
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        List<Long> roles) {
}
