package io.unityfoundation.auth;

import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotBlank;
import at.favre.lib.crypto.bcrypt.BCrypt;

@Singleton
class BCryptPasswordEncoder implements PasswordEncoder {

  private final BCrypt.Verifyer pwdVerifier = BCrypt.verifyer();

  public String encode(@NotBlank @NonNull String rawPassword) {
    char[] passwordCharacters = rawPassword.toCharArray();
    return hashPassword(passwordCharacters);
  }

  private String hashPassword(char[] passwordCharacters) {
    return BCrypt.withDefaults().hashToString(10, passwordCharacters);
  }

  @Override
  public boolean matches(@NotBlank @NonNull String rawPassword,
      @NotBlank @NonNull String encodedPassword) {
    BCrypt.Result verificationResult = verifyPassword(rawPassword, encodedPassword);
    return verificationResult.verified;
  }

  private BCrypt.Result verifyPassword(String rawPassword, String encodedPassword) {
    return pwdVerifier.verify(rawPassword.getBytes(), encodedPassword.getBytes());
  }
}