package io.unityfoundation.auth;

import jakarta.validation.constraints.NotBlank;

public interface PasswordEncoder {

  String encode(@NotBlank String rawPassword);

  /**
   * Checks if the provided raw password matches the encoded password.
   *
   * @param rawPassword The raw password that needs to be checked.
   * @param encodedPassword The encoded password to compare against.
   * @return {@code true} if the raw password matches the encoded password, otherwise {@code false}.
   */
  boolean matches(@NotBlank String rawPassword,
      @NotBlank String encodedPassword);
}