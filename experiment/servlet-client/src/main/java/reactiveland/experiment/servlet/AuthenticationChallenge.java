package reactiveland.experiment.servlet;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AuthenticationChallenge(
        @NotBlank
        String id,
        @NotBlank
        String nonce,
        @NotNull
        Instant expiresAt,
        @NotNull
        States state,
        @Nullable
        String customerId) {
    public enum States {
        AWAITING_CAPTURE, CAPTURED, SIGNED, DEAD
    }
}
