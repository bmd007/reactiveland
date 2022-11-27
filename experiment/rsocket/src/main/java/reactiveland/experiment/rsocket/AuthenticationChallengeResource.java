package reactiveland.experiment.rsocket;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.text.ParseException;
import java.util.Optional;

import static reactiveland.experiment.rsocket.AuthenticationChallenge.States.SIGNED;

@Slf4j
@Controller
public class AuthenticationChallengeResource {

    private static final String A_CUSTOMER_PUBLIC_KEY = """
                -----BEGIN PUBLIC KEY-----
                MIICITANBgkqhkiG9w0BAQEFAAOCAg4AMIICCQKCAgBKWLo9XZOnDfTZbIYB2fqT
                WVVkWyx46DQ2/3cEWQIzcCR5MFwLIuW/zEySj2sF7pngzA2NZfvLxaecf3xiiCTi
                ptDUbLLNVDClDS149SnP0oAO6fpEv0BNJt4aK7Yrs4o3u0iJeLZ9mZECcJki3vWz
                TTEKCUgl29ZAz/2MbXC8S+LCQMgP/lxlYMTOKoPV/1L624nhxVLE7L5qHzUQXITZ
                mmxwfdMVbSNzUIXXAweHwzQ++SoCN0/vVkhUK5Pl+HTXkVMIcEbglYXKipNW5NBD
                I/rpxflzzosHtI6ErXkFGAEn84xHuUiagCyt+OQITrgWacEI2Tc8u9aryKeS6m3R
                7Z9To/HxC7dYTf4gh8HJvunzgx06AsjpfL8mlel7o9uj5Ym5025xt+yb0nQ7tH+d
                P0OsNLcGJO1XCpARS/Y9TM2Nha5cX5PdgbcG/bAL+J22l+HBgaQIuvvya/qUJV9l
                1TxxHFsUKJaS3vKTYteaF+WrvTJlqBaVEBGjOlPczyUbl76EOSyxDnagLx6FXmOb
                8liUUxIqWqYHk1hJ091vYiyd5/Lx/ol7otNw2y3v6DhKMRFioJxpjWQ8RCN8ZcVW
                O8qOaAQyPnIMhuiurFKu75QFNpjsccHc4GhrExzYn8mifiRN5LtpbRieaHQEtHyC
                x5hMf7WkGfKxvDKvyJBXGwIDAQAB
                -----END PUBLIC KEY-----
            """.trim();

    public static final Mono<AuthenticationChallenge> NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION =
            Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "challenge not found or dead"));

    private final AuthenticationChallengeRepository repository;
    private final R2dbcEntityOperations dbOps;

    public AuthenticationChallengeResource(AuthenticationChallengeRepository repository, R2dbcEntityOperations dbOps) {
        this.dbOps = dbOps;
        this.repository = repository;
    }

    @MessageMapping("/challenges")
    public Flux<AuthenticationChallenge> getChallenges() {
        return repository.findAll();
    }

    @MessageMapping("/challenges/{id}")
    public Mono<AuthenticationChallenge> getChallenge(@DestinationVariable String id) {
        return repository.findById(id)
                .filter(AuthenticationChallenge::isAlive)
                .switchIfEmpty(NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION);
    }

    @MessageMapping("/challenges/request")
    public Flux<AuthenticationChallenge> challengeMachine() {
        return Flux.<AuthenticationChallenge>generate(sink -> sink.next(AuthenticationChallenge.createNew()))
                .flatMap(dbOps::insert);
    }

    @MessageMapping("/challenges/response")
    public Flux<AuthenticationChallenge> challengeResponse(@RequestBody Flux<ChallengeResponse> challengeResponses) {
        return challengeResponses
                .map(challengeResponse -> {
                    try {
                        return SignedJWT.parse(challengeResponse.jwt);
                    } catch (ParseException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad jwt", e);
                    }
                })
                .map(signedJWT -> {
                    try {
                        String challengeId = Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("id")).orElseThrow();
                        var publicKey = JWK.parseFromPEMEncodedObjects(A_CUSTOMER_PUBLIC_KEY).toRSAKey();
                        var verifier = new RSASSAVerifier(publicKey);
                        boolean isJwtVerified = signedJWT.verify(verifier);
                        return Tuples.of(challengeId, isJwtVerified);
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jwt is missing claim id", e);
                    }
                })
                .flatMap(challengeIdAndIsVerified -> {
                    if (Boolean.TRUE.equals(challengeIdAndIsVerified.getT2())) {
                        return Mono.just(challengeIdAndIsVerified.getT1());
                    }
                    return repository.deleteById(challengeIdAndIsVerified.getT1())
                            .flatMap(ignore ->
                                    Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid signature")));
                })
                .flatMap(repository::findById)
                .filter(AuthenticationChallenge::isAlive)
                .switchIfEmpty(NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION)
                .flatMap(challenge -> repository.save(challenge.sign("RANDOM_CUSTOMER_ID")));
    }

    record AuthenticateRequestBody(String challengeId, String nonce) {
    }

    @MessageMapping("/challenges/authenticate")
    public Flux<String> authenticate(Flux<AuthenticateRequestBody> requestBodies) {
        return requestBodies.flatMap(requestBody ->
                        repository.findById(requestBody.challengeId)
                                .delayUntil(repository::delete)
                                .filter(AuthenticationChallenge::isAlive)
                                .filter(cd -> SIGNED.equals(cd.state))
                                .switchIfEmpty(NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION)
                                .filter(ch -> ch.authenticate(requestBody.nonce)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid nonce")))
                .map(AuthenticationChallenge::getCustomerId);
    }

    @MessageMapping("/challenges/states/captured")
    public Flux<AuthenticationChallenge> moveChallengeStateToCaptured(Flux<String> ids) {
        return ids.flatMap(repository::findById)
                .map(AuthenticationChallenge::capture)
                .switchIfEmpty(NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION)
                .flatMap(repository::save);
    }

    @MessageMapping("/challenges/delete/all")
    public void deleteAll() {
        repository.deleteAll().subscribe();
    }

    record ChallengeResponse(String jwt) {
    }
}
