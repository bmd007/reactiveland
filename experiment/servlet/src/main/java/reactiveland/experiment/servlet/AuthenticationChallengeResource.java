package reactiveland.experiment.servlet;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
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

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static reactiveland.experiment.servlet.AuthenticationChallenge.States.AWAITING_CAPTURE;
import static reactiveland.experiment.servlet.AuthenticationChallenge.States.CAPTURED;
import static reactiveland.experiment.servlet.AuthenticationChallenge.States.SIGNED;


@Slf4j
@RestController
@RequestMapping("challenges")
public class AuthenticationChallengeResource {

    private static final Map<AuthenticationChallenge.States, HttpStatus> CHALLENGE_STATE_TO_HTTP_CODE_MAP =
            Map.of(AWAITING_CAPTURE, HttpStatus.CREATED,
                    CAPTURED, HttpStatus.ACCEPTED,
                    SIGNED, HttpStatus.OK);

    private static final String A_CUSTOMER_PUBLIC_KEY = """
                -----BEGIN PUBLIC KEY-----
                MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCzLkmmMTR4XtbaUd7HJHzoTiAu
                K99j27MXhBMPL7FDK1nvmqBjvI8MSSIvPjRfYU9wkX5E6xi5DTu/597Pfj0x8q6m
                b5Cyfm+rx/dzrdBHhfaKHM4jl6o3bk9wt1NOtBDWftPFW5SRh09yjc/TuCMHg4VP
                wsbFOmA0rHXrEbu/cQIDAQAB
                -----END PUBLIC KEY-----
            """.trim();

    private static final String A_CUSTOMER_PRIVATE_KEY = """
               -----BEGIN RSA PRIVATE KEY-----
               MIICXQIBAAKBgQCzLkmmMTR4XtbaUd7HJHzoTiAuK99j27MXhBMPL7FDK1nvmqBj
               vI8MSSIvPjRfYU9wkX5E6xi5DTu/597Pfj0x8q6mb5Cyfm+rx/dzrdBHhfaKHM4j
               l6o3bk9wt1NOtBDWftPFW5SRh09yjc/TuCMHg4VPwsbFOmA0rHXrEbu/cQIDAQAB
               AoGAG5HvuyavECZnoMggIzw2C/iZcwFFKjRP5jpoRFnuSIuPFxMPwsjsqdNG80X7
               AQIUGxoH98rEzxR+MRUYb4zZFWexZZ1kYx37+WcTEQMwA6FyL+htWFU0d6HDfQv0
               WWYMcoLvsMaQqszVyRW7ceSlhJXqAxZwDHrUA7seb2EmiVECQQD6JM28S4jMEECA
               /KLY9FQCTpUAoS02mmqmkDJBloBIG7QTwlObIxNFAiqBzdvmqplt55dC6ndIF9mw
               bM50G+3rAkEAt2At09Dg7c0A+1C46SR8XNGorisdgvVGYH7+s9lOg144rDSWQFse
               Qkl5tzPWTi9i7b5ktjXLfK28Y9z6GaqFEwJBAPG+L7YRqZrM+gmuHhNdzPKNzyJU
               ocVrZjailG8ea8tEOrv9yZ7cPvsqJLpdoG9D4BN/BYf94Fkj85W1EbDUbRECQE/U
               DN8zBVhAcHb3cyf7fDAkDVyU5GoIQLTtVBATP7ysndtJoUcu44NT3SrF5DtxIY4B
               3nH8BTOnpmWK402dEAUCQQDoQP3Myxb40/S8guVA4MVzAIJ4uq17ZLxA0w4w+dsQ
               7RA5p4sU4VnZcBUwMYMom+utqFqxNsA/DMGYYJsH50bN
               -----END RSA PRIVATE KEY-----
            """.trim();
    public static final ResponseStatusException NOT_FOUND_OR_DEAD_UNATHOTIZED_EXCEPTION = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "challenge not found or dead");

    private final KeyFactory keyFactory;
    private final AuthenticationChallengeRepository repository;

    public AuthenticationChallengeResource(KeyFactory keyFactory, AuthenticationChallengeRepository repository) {
        this.keyFactory = keyFactory;
        this.repository = repository;
    }

    @GetMapping
    public AuthenticationChallenges getChallenges() {
        return new AuthenticationChallenges(repository.findAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<AuthenticationChallenge> getChallenge(@PathVariable(name = "id") String id) {
        return repository.findById(id)
                .filter(AuthenticationChallenge::isAlive)
                .map(authenticationChallenge ->
                        new ResponseEntity<>(authenticationChallenge,
                                CHALLENGE_STATE_TO_HTTP_CODE_MAP.get(authenticationChallenge.getState())))
                .orElseThrow(() -> NOT_FOUND_OR_DEAD_UNATHOTIZED_EXCEPTION);
    }

    @PostMapping
    public ResponseEntity<AuthenticationChallenge> challengeMachine() {
        AuthenticationChallenge authenticationChallenge = repository.save(AuthenticationChallenge.createNew());
        return new ResponseEntity<>(authenticationChallenge,
                CHALLENGE_STATE_TO_HTTP_CODE_MAP.get(authenticationChallenge.getState()));
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("response")
    public AuthenticationChallenge challengeResponse(@RequestBody ChallengeResponse challengeResponse) throws ParseException, InvalidKeySpecException, JOSEException {
        final SignedJWT signedJWT = SignedJWT.parse(challengeResponse.jwt);
        final String id = Optional.ofNullable(signedJWT.getJWTClaimsSet().getStringClaim("id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "jwt is missing claim id"));
        AuthenticationChallenge challenge = repository.findById(id)
                .filter(AuthenticationChallenge::isAlive)
                .orElseThrow(() -> NOT_FOUND_OR_DEAD_UNATHOTIZED_EXCEPTION);
//        final String deviceId = getClaim(signedJWT, "sub");
//        EnrollmentEntity enrollmentEntity = enrollmentService.getEnrollment(deviceId).orElseThrow(() -> {
//            log.warn("Device not found {} {}", kv(KEY_DEVICE_ID, deviceId), kv(KEY_SIGNING_NONCE, signingNonce));
//            challengeRepository.delete(signingNonce);
//            throw new NoSuchElementException("Device not found: " + deviceId);
//        });
//        if (enrollmentEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
//            log.warn("Enrollment expired {} {}", kv(KEY_DEVICE_ID, enrollmentEntity.getDeviceId()), kv(KEY_SIGNING_NONCE, signingNonce));
//            challengeRepository.delete(signingNonce);
//            throw new InvalidEnrollmentException(enrollmentEntity.getDeviceId());
//        }
        byte[] encoded = Base64.getDecoder().decode(A_CUSTOMER_PUBLIC_KEY);
        var keySpec = new X509EncodedKeySpec(encoded);
        var publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        if (!signedJWT.verify(new RSASSAVerifier(publicKey))) {
            log.warn("Invalid signature with JWT {}", challengeResponse.jwt);
            repository.save(challenge.kill());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid signature");
        }
        return repository.save(challenge.sign("RANDOM_CUSTOMER_ID"));
    }

    @PostMapping("{id}/authenticate/{nonce}")
    public String authenticate(@PathVariable String id, @PathVariable String nonce) {
        AuthenticationChallenge challenge = repository.findById(id)
                .filter(AuthenticationChallenge::isAlive)
                .orElseThrow(() -> NOT_FOUND_OR_DEAD_UNATHOTIZED_EXCEPTION);
        repository.save(challenge.kill());
        if (!challenge.authenticate(nonce)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid nonce or state");
        }
        return challenge.customerId;
    }

    @PutMapping("{id}/states/captured")
    public AuthenticationChallenge moveChallengeStateToCaptured(@PathVariable(name = "id") String id) {
        return repository.findById(id)
                .map(AuthenticationChallenge::capture)
                .map(repository::save)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "challenge not found"));
    }

    @DeleteMapping
    public void deleteAll() {
        repository.deleteAll();
    }

    record ChallengeResponse(String jwt) {
    }

    record AuthenticationChallenges(List<AuthenticationChallenge> authenticationChallenges) {
    }
}
