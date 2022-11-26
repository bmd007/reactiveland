package reactiveland.experiment.servlet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AuthenticationChallenge(@NotBlank String nonce, @NotNull Instant expiresAt, @NotNull States state) {
}
