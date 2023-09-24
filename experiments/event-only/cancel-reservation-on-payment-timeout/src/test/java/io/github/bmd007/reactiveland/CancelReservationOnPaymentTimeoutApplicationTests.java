package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.dto.TableReservationDto;
import io.github.bmd007.reactiveland.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {Topics.RESERVATION_EVENTS_TOPIC, Topics.CUSTOMER_EVENTS_TOPIC})
class CancelReservationOnPaymentTimeoutApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(CancelReservationOnPaymentTimeoutApplicationTests.class);

    @Autowired
    private KafkaEventProducer kafkaEventProducer;


    @LocalServerPort
    private int localServerPort;

    private WebClient webClient;

    @BeforeEach
    public void beforeEach() {
        webClient = WebClient.create("http://localhost:%s".formatted(localServerPort));
    }

    @Test
    void contextLoads() {
        //given
        Flux<String> booleanFlux = Flux.range(0, 9)
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .delayUntil(integer -> reserveAndPayForTable())
                .flatMap(integer ->
                        switch (integer % 3) {
                            case 0 -> reserveAndPayForTable();
                            case 1 -> reserveTableAndPayLate();
                            case 2 -> reserveTableAndLeave();
                            default -> Flux.error(new IllegalStateException("Unexpected value: " + integer % 3));
                        }
                ).log();
        //when
        StepVerifier.create(booleanFlux)
                //then
                .expectNextCount(7)
                .expectComplete()
                .verify();
    }

    private Mono<String> reserveAndPayForTable() {
        String customerId = UUID.randomUUID().toString();
        long delay = 5L;
        String tableId = UUID.randomUUID().toString();
        String paymentId = UUID.randomUUID().toString();
        log.info("reserveAndPayForTable for {}", customerId);
        return Mono.just(new Event.CustomerEvent.CustomerRequestedTable(customerId, tableId))
                .flatMap(event -> kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC))
                .delayElement(Duration.ofSeconds(delay))
                .map(ignored -> new Event.CustomerEvent.CustomerPaidForReservation(customerId, paymentId))
                .flatMap(event -> kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC))
                .delayElement(Duration.ofSeconds(10))
                .flatMap(ignored -> webClient.get()
                        .uri("/api/tables/reservations/%s".formatted(customerId))
                        .retrieve()
                        .bodyToMono(TableReservationDto.class)
                )
                .map(TableReservationDto::status)
                .map(s -> customerId + ":" + s);
    }

    private Mono<String> reserveTableAndLeave() {
        String customerId = UUID.randomUUID().toString();
        long delay = 22L;
        String tableId = UUID.randomUUID().toString();
        log.info("reserveTableAndLeave for {}", customerId);
        return Mono.just(new Event.CustomerEvent.CustomerRequestedTable(customerId, tableId))
                .flatMap(event -> kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC))
                .delayElement(Duration.ofSeconds(delay))
                .flatMap(ignored -> webClient.get()
                        .uri("/api/tables/reservations/%s".formatted(customerId))
                        .retrieve()
                        .bodyToMono(TableReservationDto.class)
                )
                .map(TableReservationDto::status)
                .map(s -> customerId + ":" + s);
    }

    private Mono<String> reserveTableAndPayLate() {
        String customerId = UUID.randomUUID().toString();
        long delay = 20L;
        String tableId = UUID.randomUUID().toString();
        String paymentId = UUID.randomUUID().toString();
        log.info("reserveTableAndPayLate for {}", customerId);
        return Mono.just(new Event.CustomerEvent.CustomerRequestedTable(customerId, tableId))
                .flatMap(event -> kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC))
                .delayElement(Duration.ofSeconds(delay))
                .map(ignored -> new Event.CustomerEvent.CustomerPaidForReservation(customerId, paymentId))
                .flatMap(event -> kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC))
                .delayElement(Duration.ofSeconds(10))
                .flatMap(ignored -> webClient.get()
                        .uri("/api/tables/reservations/%s".formatted(customerId))
                        .retrieve()
                        .bodyToMono(String.class)
                        .onErrorResume(WebClientResponseException.class, exception -> Mono.just(exception.getStatusCode().toString()))
                )
                .map(httpStatusCode -> httpStatusCode.equals(HttpStatus.NOT_FOUND.toString()))
                .map(s -> customerId + ":" + s);
    }

}
