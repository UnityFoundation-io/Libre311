package io.unityfoundation.auth;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.serde.annotation.Serdeable;
import io.unityfoundation.auth.entities.PasswordResetToken;
import io.unityfoundation.auth.entities.PasswordResetTokenRepo;
import io.unityfoundation.auth.entities.User;
import io.unityfoundation.auth.entities.UserRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Secured("INTERNAL_SERVICE")
@Controller("/password-reset")
public class PasswordResetController {

    private final UserRepo userRepo;
    private final PasswordResetTokenRepo tokenRepo;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepo userRepo, PasswordResetTokenRepo tokenRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Post("/generate")
    public HttpResponse<GenerateTokenResponse> generateToken(@Body @Valid GenerateTokenRequest request) {
        Optional<User> userOptional = userRepo.findByEmail(request.email());
        if (userOptional.isEmpty()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User user = userOptional.get();
        tokenRepo.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(sha256(rawToken));
        token.setUserId(user.getId());
        token.setExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
        tokenRepo.save(token);

        return HttpResponse.ok(new GenerateTokenResponse(rawToken));
    }

    @Post("/reset")
    @Transactional
    public HttpResponse<?> resetPassword(@Body @Valid ResetPasswordRequest request) {
        Optional<PasswordResetToken> tokenOptional = tokenRepo.findByToken(sha256(request.token()));

        if (tokenOptional.isEmpty()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }

        PasswordResetToken token = tokenOptional.get();
        if (token.getExpiry().isBefore(Instant.now())) {
            tokenRepo.delete(token);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        Optional<User> userOptional = userRepo.findById(token.getUserId());
        if (userOptional.isEmpty()) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found");
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.update(user);

        tokenRepo.delete(token);

        return HttpResponse.ok();
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Serdeable
    public record GenerateTokenRequest(@NotBlank String email) {}

    @Serdeable
    public record GenerateTokenResponse(@NotBlank String token) {}

    @Serdeable
    public record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword) {}
}
