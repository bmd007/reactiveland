package reactiveland.experiment.webflux;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.nimbusds.jose.JWSAlgorithm.RS256;

@Slf4j
@SpringBootApplication
public class WebfluxClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxClientApplication.class, args);
	}
	private static final String A_CUSTOMER_PRIVATE_KEY = """
               -----BEGIN RSA PRIVATE KEY-----
               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
               -----END RSA PRIVATE KEY-----
            """.trim();

	private final WebClient webClient;

	public WebfluxClientApplication(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder
				.codecs(codec -> codec.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
				.baseUrl("http://webflux")
				.build();
	}

	@EventListener(ApplicationReadyEvent.class)
	public void start() {
			deleteAllPreviousChallenges().block();
			Flux.range(0, 200000)
					.subscribeOn(Schedulers.boundedElastic())
					.flatMap(ignore -> askForChallenge())
					.flatMap(this::captureChallenge)
					.flatMap(this::respondToChallenge)
					.flatMap(this::authenticateUsingChallenge)
					.filter(customerId -> !customerId.isEmpty())
					.doOnError(throwable -> Metrics.counter("reactiveland_experiment_webflux_authentication_error").increment())
					.doOnNext(ignore -> Metrics.counter("reactiveland_experiment_webflux_one_round_success").increment())
					.onErrorReturn("ERROR")
					.subscribe();
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
				.doOnNext(challenges -> Metrics.counter("reactiveland_experiment_webflux_challenged").increment())
				.doOnError(error -> log.error("error while asking for a challenge", error));
	}

	private Mono<AuthenticationChallenge> captureChallenge(AuthenticationChallenge challenge) {
		return webClient
				.put()
				.uri("challenges/{id}/states/captured", challenge.id())
				.retrieve()
				.bodyToMono(AuthenticationChallenge.class)
				.doOnNext(challenges -> Metrics.counter("reactiveland_experiment_webflux_captured").increment())
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
					.bodyValue(new ChallengeResponse(signedJWT.serialize()))
					.retrieve()
					.bodyToMono(AuthenticationChallenge.class)
					.doOnNext(challenges -> Metrics.counter("reactiveland_experiment_webflux_responded").increment())
					.doOnError(error -> log.error("error while signing challenges {}", challenge, error));
		} catch (Exception e) {
			return Mono.error(e);
		}
	}

	private Mono<String> authenticateUsingChallenge(AuthenticationChallenge challenge) {
		return webClient
				.post()
				.uri("challenges/{id}/authenticate/{nonce}", challenge.id(), challenge.nonce())
				.retrieve()
				.bodyToMono(String.class)
				.doOnNext(challenges -> Metrics.counter("reactiveland_experiment_webflux_authenticated").increment())
				.doOnError(error -> log.error("error while authenticating using challenges {}", challenge, error));
	}
}
