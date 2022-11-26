package reactiveland.experiment.servlet;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import static com.nimbusds.jose.JWSAlgorithm.RS256;

@Slf4j
@SpringBootApplication
public class ServletClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServletClientApplication.class, args);
    }

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

    private final WebClient webClient;
    private final KeyFactory keyFactory;

    public ServletClientApplication(WebClient.Builder webClientBuilder) throws NoSuchAlgorithmException {
        this.keyFactory = KeyFactory.getInstance("RSA");
        this.webClient = webClientBuilder
                .codecs(codec -> codec.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .baseUrl("http://servlet")
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        deleteAllPreviousChallenges().block();
        for (int i = 0; i < 100_000; i++) {
            askForChallenge()
                    .flatMap(this::captureChallenge)
                    .flatMap(this::respondToChallenge)
                    .flatMap(this::authenticateUsingChallenge)
                    .filter(customerId -> !customerId.isEmpty())
                    .doOnError(throwable -> Metrics.counter("reactiveland_experiment_servlet_authentication_error").increment())
                    .doOnNext(challenges -> Metrics.counter("reactiveland_experiment_servlet_authentication_success").increment())
                    .block();
        }
    }

    private Mono<Integer> deleteAllPreviousChallenges() {
        return webClient
                .delete()
                .uri("challenges")
                .retrieve()
                .bodyToMono(Integer.class)
                .doOnError(error -> log.error("error while deleting challenge", error))
                .doOnNext(nonce -> log.info("challenge is deleted"));
    }

    private Mono<AuthenticationChallenge> askForChallenge() {
        return webClient
                .post()
                .uri("challenges")
                .retrieve()
                .bodyToMono(AuthenticationChallenge.class)
                .doOnError(error -> log.error("error while asking for a challenge", error));
    }

    private Mono<AuthenticationChallenge> captureChallenge(AuthenticationChallenge challenge) {
        return webClient
                .put()
                .uri("challenges/{id}/states/captured", challenge.id())
                .retrieve()
                .bodyToMono(AuthenticationChallenge.class)
                .doOnError(error -> log.error("error while capturing challenges {}", challenge, error));
    }

    private Mono<AuthenticationChallenge> respondToChallenge(AuthenticationChallenge challenge) {
        try {
            RSAKey signingKey = JWK.parseFromPEMEncodedObjects(A_CUSTOMER_PRIVATE_KEY).toRSAKey();
            JWSSigner signer = new RSASSASigner(signingKey);
            JWSHeader jwsHeader = new JWSHeader.Builder(RS256)
                    .keyID(signingKey.getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();
            var jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject("RANDOM_DEVICE_ID")
                    .claim("id", challenge.id())
                    .build();
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
            signedJWT.sign(signer);
            return webClient
                    .post()
                    .uri("challenges/response")
                    .bodyValue(signedJWT)
                    .retrieve()
                    .bodyToMono(AuthenticationChallenge.class)
                    .doOnError(error -> log.error("error while signing challenges {}", challenge, error));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<String> authenticateUsingChallenge(AuthenticationChallenge challenge) {
        return webClient
                .put()
                .uri("challenges/{id}/authenticate/{nonce}", challenge.id(), challenge.nonce())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("error while authenticating using challenges {}", challenge, error));
    }
}
