package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.dto.TableReservationDto;
import io.github.bmd007.reactiveland.event.Event;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
class ApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(ApplicationTests.class);

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
    void paymentTimeoutDetectionTest() {
        //given
        Flux<ExperimentResult> booleanFlux = Flux.range(0, 20)
                .subscribeOn(Schedulers.parallel())
                .publishOn(Schedulers.parallel())
                .flatMap(integer ->
                        switch (integer % 3) {
                            case 0 -> reserveAndPayForTable();
                            case 1 -> reserveTableAndPayLate();
                            case 2 -> reserveTableAndLeave();
                            default -> Flux.error(new IllegalStateException("Unexpected value: " + integer % 3));
                        }
                )
                .log()
                .filter(ExperimentResult::wasSuccessful);
        //when
        StepVerifier.create(booleanFlux)
                //then
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    @Test
    void reserveAndPayForTableSingleTry() {
        //given
        Mono<ExperimentResult> booleanFlux = reserveAndPayForTable().log()
                .filter(ExperimentResult::wasSuccessful);
        //when
        StepVerifier.create(booleanFlux)
                //then
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    void reserveTableAndPayLateSingleTry() {
        //given
        Mono<ExperimentResult> booleanFlux = reserveTableAndPayLate().log()
                .filter(ExperimentResult::wasSuccessful);
        //when
        StepVerifier.create(booleanFlux)
                //then
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    void reserveTableAndLeaveSingleTry() {
        //given
        Mono<ExperimentResult> booleanFlux = reserveTableAndLeave().log()
                .filter(ExperimentResult::wasSuccessful);
        //when
        StepVerifier.create(booleanFlux)
                //then
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    private Mono<ExperimentResult> reserveAndPayForTable() {
        String customerId = UUID.randomUUID().toString();
        long delay = 5L;
        String tableId = UUID.randomUUID().toString();
        return requestTable(customerId, tableId)
                .delayElement(Duration.ofSeconds(delay))
                .flatMap(ignored -> payForTable(customerId, tableId))
                .delayElement(Duration.ofSeconds(2))
                .flatMap(ignored -> fetchReservationStatus(customerId))
                .onErrorResume(WebClientResponseException.class, exception -> Mono.just(exception.getStatusCode().toString()))
                .map(status -> new ExperimentResult(customerId, status, "reserveAndPayForTable"));
    }

    private Mono<String> fetchReservationStatus(String customerId) {
        return webClient.get()
                .uri("/api/tables/reservations/%s".formatted(customerId))
                .retrieve()
                .bodyToMono(TableReservationDto.class)
                .map(TableReservationDto::status)
                .onErrorResume(WebClientResponseException.class, exception -> Mono.just(exception.getStatusCode().toString()));
    }

    private Mono<ExperimentResult> reserveTableAndLeave() {
        String customerId = UUID.randomUUID().toString();
        long delay = 22L;
        String tableId = UUID.randomUUID().toString();
        return requestTable(customerId, tableId)
                .delayElement(Duration.ofSeconds(delay))
                .flatMap(ignored -> fetchReservationStatus(customerId))
                .onErrorResume(WebClientResponseException.class, exception -> Mono.just(exception.getStatusCode().toString()))
                .map(status -> new ExperimentResult(customerId, status, "reserveTableAndLeave"));
    }

    private Mono<ExperimentResult> reserveTableAndPayLate() {
        String customerId = UUID.randomUUID().toString();
        long delay = 20L;
        String tableId = UUID.randomUUID().toString();
        return requestTable(customerId, tableId)
                .delayElement(Duration.ofSeconds(delay))
                .flatMap(ignored -> payForTable(customerId, tableId))
                .delayElement(Duration.ofSeconds(1))
                .flatMap(ignored -> fetchReservationStatus(customerId))
                .map(status -> new ExperimentResult(customerId, status, "reserveTableAndPayLate"));
    }

    private Mono<RecordMetadata> requestTable(String customerId, String tableId) {
        var event = new Event.CustomerEvent.CustomerRequestedTable(customerId, tableId);
        return kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC);
    }

    private Mono<RecordMetadata> payForTable(String customerId, String tableId) {
        var event = new Event.CustomerEvent.CustomerPaidForTable(customerId, tableId);
        return kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC);
    }

}
