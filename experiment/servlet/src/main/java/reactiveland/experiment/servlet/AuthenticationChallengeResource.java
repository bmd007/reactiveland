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
import java.security.NoSuchAlgorithmException;
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

    public static final ResponseStatusException NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "challenge not found or dead");

    private final KeyFactory keyFactory;
    private final AuthenticationChallengeRepository repository;

    public AuthenticationChallengeResource(AuthenticationChallengeRepository repository) throws NoSuchAlgorithmException {
        this.keyFactory = KeyFactory.getInstance("RSA");
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
                .orElseThrow(() -> NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION);
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
                .orElseThrow(() -> NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION);
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
                .orElseThrow(() -> NOT_FOUND_OR_DEAD_UNAUTHORIZED_EXCEPTION);
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
