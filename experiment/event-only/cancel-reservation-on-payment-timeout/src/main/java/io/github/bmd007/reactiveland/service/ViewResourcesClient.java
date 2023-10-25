package io.github.bmd007.reactiveland.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * This Client calls the resources defined in this package.
 * This behaviour is due to partitioning of the kafka and limited access of each
 * KafkaStream application instance to each view (KTABLE). So in order to query each KTABLE,
 * if the requested data is not accessible for the ?answering? instance, using this client,
 * that instance will redirect the question to another instance.
 * The port and ip address of each instance is configured as a property of kafkaStream; so each instance
 * is aware that who has access to what portion of data (by fetching metaData from kafka).
 */
@Component
public class ViewResourcesClient {

    private final WebClient.Builder webClientBuilder;

    public ViewResourcesClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public <T> Mono<T> getOne(Class<T> bodyType, String url) {
        return webClientBuilder
                .build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(bodyType)
                .onErrorResume(this::handleClientError);
    }

    Mono handleClientError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException webClientResponseException &&
                webClientResponseException.getStatusCode().is4xxClientError()) {
            return Mono.empty();
        } else {
            return Mono.error(throwable);
        }
    }
}
