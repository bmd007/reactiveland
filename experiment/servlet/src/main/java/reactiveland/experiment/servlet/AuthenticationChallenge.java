package reactiveland.experiment.servlet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@JsonDeserialize(builder = AuthenticationChallenge.AuthenticationChallengeBuilder.class)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AuthenticationChallenge {

    @Id
    @NotBlank
    String nonce;
    @NotNull
    Instant expiresAt;
    @NotNull
    States state;

    public static AuthenticationChallenge sendNew() {
        return AuthenticationChallenge.builder()
                .nonce(UUID.randomUUID().toString())
                .state(States.AWAITING_CAPTURE)
                .expiresAt(Instant.now().plusSeconds(30))
                .build();
    }

    public AuthenticationChallenge capture() {
        if (this.getState().equals(States.AWAITING_CAPTURE)) {
            return toBuilder()
                    .state(States.CAPTURED)
                    .expiresAt(Instant.now().plusSeconds(300))
                    .build();
        }
        throw new IllegalStateException("wrong state transition is requested. Only SENT challenges can be captured");
    }

    public AuthenticationChallenge sign() {
        if (this.getState().equals(States.CAPTURED) || this.getState().equals(States.AWAITING_CAPTURE)) {
            return toBuilder()
                    .state(States.SIGNED)
                    .expiresAt(Instant.now().plusSeconds(100))
                    .build();
        }
        throw new IllegalStateException("wrong state transition is requested. Only CAPTURED challenges can be signed");
    }

    public AuthenticationChallenge kill() {
        return toBuilder().state(States.DEAD).expiresAt(Instant.EPOCH).build();
    }

    public enum States {
        AWAITING_CAPTURE, CAPTURED, SIGNED, DEAD
    }
}
