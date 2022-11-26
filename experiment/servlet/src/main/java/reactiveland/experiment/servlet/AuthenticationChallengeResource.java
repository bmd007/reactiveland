package reactiveland.experiment.servlet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static reactiveland.experiment.servlet.AuthenticationChallenge.States.AWAITING_CAPTURE;
import static reactiveland.experiment.servlet.AuthenticationChallenge.States.CAPTURED;
import static reactiveland.experiment.servlet.AuthenticationChallenge.States.DEAD;
import static reactiveland.experiment.servlet.AuthenticationChallenge.States.SIGNED;


@Slf4j
@RestController
@RequestMapping("challenges")
public class AuthenticationChallengeResource {

    static final Map<AuthenticationChallenge.States, HttpStatus> CHALLENGE_STATE_TO_HTTP_CODE_MAP =
            Map.of(AWAITING_CAPTURE, HttpStatus.CREATED,
                    CAPTURED, HttpStatus.ACCEPTED,
                    SIGNED, HttpStatus.OK,
                    DEAD, HttpStatus.UNAUTHORIZED);

    private final AuthenticationChallengeRepository repository;

    public AuthenticationChallengeResource(AuthenticationChallengeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public AuthenticationChallenges getChallenges() {
        return new AuthenticationChallenges(repository.findAll().stream()
                .filter(challenge -> !challenge.getState().equals(DEAD))
                .collect(Collectors.toList()));
    }

    @GetMapping("{nonce}")
    public ResponseEntity<AuthenticationChallenge>  getChallenge(@PathVariable(name = "nonce") String nonce) {
        return repository.findById(nonce)
                .filter(challenge -> !challenge.getState().equals(DEAD))
                .map(authenticationChallenge ->
                        new ResponseEntity<>(authenticationChallenge,
                                CHALLENGE_STATE_TO_HTTP_CODE_MAP.get(authenticationChallenge.getState())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<AuthenticationChallenge> challengeMachine() {
        AuthenticationChallenge authenticationChallenge = repository.save(AuthenticationChallenge.sendNew());
        return new ResponseEntity<>(authenticationChallenge,
                CHALLENGE_STATE_TO_HTTP_CODE_MAP.get(authenticationChallenge.getState()));
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("{nonce}/response")
    public AuthenticationChallenge challengeResponse(@PathVariable(name = "nonce") String nonce,
                                                     @RequestBody ChallengeResponse challengeResponse) {
        Optional<AuthenticationChallenge> challengeByIdOptional = repository.findById(nonce);
        if (challengeByIdOptional.isPresent() && challengeByIdOptional.get().getState().equals(SIGNED)) {
            killTheChallenge(challengeByIdOptional.get().getNonce());
        }
        AuthenticationChallenge readyToSignChallenge = challengeByIdOptional
                .filter(challenge -> !Set.of(DEAD, SIGNED).contains(challenge.getState()))
                .filter(challenge -> challenge.getState().equals(CAPTURED) || challenge.getState().equals(AWAITING_CAPTURE))
                .filter(challenge -> isChallengeResponseValid(challenge, challengeResponse))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid nonce"));
        try {
            return repository.save(readyToSignChallenge.sign());
        } catch (Exception e) {
            killTheChallenge(nonce);
            throw e;
        }
    }

    @PutMapping("{nonce}/states/captured")
    public AuthenticationChallenge moveChallengeStateToCaptured(@PathVariable(name = "nonce") String nonce) {
        Optional<AuthenticationChallenge> challengeByIdOptional = repository.findById(nonce);
        if (challengeByIdOptional.isPresent() && challengeByIdOptional.get().getState().equals(CAPTURED)) {
            killTheChallenge(challengeByIdOptional.get().getNonce());
        }
        AuthenticationChallenge readyToSignChallenge = challengeByIdOptional
                .filter(challenge -> !Set.of(DEAD, SIGNED, CAPTURED).contains(challenge.getState()))
                .filter(challenge -> challenge.getState().equals(AWAITING_CAPTURE))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid nonce"));
        try {
            return repository.save(readyToSignChallenge.capture());
        } catch (Exception e) {
            killTheChallenge(nonce);
            throw e;
        }
    }

    @DeleteMapping
    public void deleteAll() {
        repository.deleteAll();
    }

    private void killTheChallenge(String nonce) {
        repository.deleteById(nonce);
    }

    private boolean isChallengeResponseValid(AuthenticationChallenge challenge, ChallengeResponse challengeResponse) {
        //todo implement
        if (challenge != null && challengeResponse != null) {
            log.error("FUCK PDM");
        }
        return true;
    }

    record ChallengeResponse(String hashedSignedValue, String data) {
    }

    record AuthenticationChallenges(List<AuthenticationChallenge> authenticationChallenges) {
    }

}
