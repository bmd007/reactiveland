package reactiveland.experiment.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static reactiveland.experiment.webflux.AuthenticationChallenge.States.AWAITING_CAPTURE;
import static reactiveland.experiment.webflux.AuthenticationChallenge.States.CAPTURED;
import static reactiveland.experiment.webflux.AuthenticationChallenge.States.DEAD;
import static reactiveland.experiment.webflux.AuthenticationChallenge.States.SIGNED;


@Slf4j
@RestController
@RequestMapping("challenges")
public class AuthenticationChallengeResource {

    Map<AuthenticationChallenge.States, HttpStatus> challengeStateToHttpCodeMap =
            Map.of(AWAITING_CAPTURE, HttpStatus.CREATED,
                    CAPTURED, HttpStatus.ACCEPTED,
                    SIGNED, HttpStatus.OK,
                    DEAD, HttpStatus.UNAUTHORIZED
            );
    private final R2dbcEntityOperations dbOps;
    private final AuthenticationChallengeRepository repository;

    public AuthenticationChallengeResource(R2dbcEntityOperations dbOps, AuthenticationChallengeRepository repository) {
        this.dbOps = dbOps;
        this.repository = repository;
    }

    @GetMapping
    public Mono<AuthenticationChallenges> getChallenges() {
        return repository.findAll()
                .filter(challenge -> !challenge.getState().equals(DEAD))
                .collectList()
                .map(AuthenticationChallenges::new);
    }

    @GetMapping("{nonce}")
    public Mono<AuthenticationChallenge> getChallenge(@PathVariable(name = "nonce") String nonce) {
        return repository.findById(nonce)
                .filter(challenge -> !challenge.getState().equals(DEAD))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PostMapping
    public Mono<ResponseEntity<AuthenticationChallenge>> challengeMachine() {
        return dbOps.insert(AuthenticationChallenge.sendNew())
                .filter(challenge -> !challenge.getState().equals(DEAD))
                .map(challenge -> new ResponseEntity<>(challenge,
                        challengeStateToHttpCodeMap.get(challenge.getState())));
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("{nonce}/response")
    public Mono<AuthenticationChallenge> challengeResponse(@PathVariable(name = "nonce") String nonce, @RequestBody ChallengeResponse challengeResponse) {
        return repository.findById(nonce)
                .doOnNext(challenge -> {
                    if (challenge.getState().equals(SIGNED)) {
                        killTheChallenge(challenge.getNonce());
                    }
                })
                .filter(challenge -> !Set.of(DEAD, SIGNED).contains(challenge.getState()))
                .filter(challenge -> challenge.getState().equals(CAPTURED) || challenge.getState().equals(AWAITING_CAPTURE))
                .filter(challenge -> isChallengeResponseValid(challenge, challengeResponse))
                .switchIfEmpty(Mono.error( new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid nonce")))
                .doOnError(error -> killTheChallenge(nonce))
                .map(AuthenticationChallenge::sign)
                .flatMap(repository::save);
    }

    @PutMapping("{nonce}/states/captured")
    public Mono<AuthenticationChallenge> moveChallengeStateToCaptured(@PathVariable(name = "nonce") String nonce) {
        return repository.findById(nonce)
                .doOnNext(challenge -> {
                    if (challenge.getState().equals(CAPTURED)) {
                        killTheChallenge(challenge.getNonce());
                    }
                })
                .filter(challenge -> !Set.of(DEAD, SIGNED, CAPTURED).contains(challenge.getState()))
                .filter(challenge -> challenge.getState().equals(AWAITING_CAPTURE))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("invalid nonce")))
                .doOnError(error -> killTheChallenge(nonce))
                .map(AuthenticationChallenge::capture)
                .flatMap(repository::save);//this is an update operation
    }

    private void killTheChallenge(String nonce) {
        repository.deleteById(nonce);
    }

    private boolean isChallengeResponseValid(AuthenticationChallenge challenge, ChallengeResponse challengeResponse) {
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
