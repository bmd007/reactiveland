package reactiveland.experiment.servlet;

import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@SpringBootApplication
public class ServletClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletClientApplication.class, args);
	}

	@Autowired WebClient.Builder webClientBuilder;

	record PossibleFileSizeRequestBody(UUID id) {
	}

	record PossibleFileSizeResponseBody(long gZipSize, long hashSize) {
	}

	@EventListener(ApplicationReadyEvent.class)
	public void start() {
		for (int i = 0; i < 1_000_000_000; i++){
			PossibleFileSizeResponseBody response = webClientBuilder.build()
					.post()
					.uri("http://servlet")
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(new PossibleFileSizeRequestBody(UUID.randomUUID()))
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.bodyToMono(PossibleFileSizeResponseBody.class)
					.block();
			Metrics.gauge("experiment.servlet.client.hashSize", response.hashSize);
			Metrics.gauge("experiment.servlet.client.gZipSize", response.gZipSize);
		}
	}
}
