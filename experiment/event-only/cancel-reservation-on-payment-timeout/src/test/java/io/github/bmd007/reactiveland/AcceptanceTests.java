package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.dto.TableReservationDto;
import io.github.bmd007.reactiveland.event.Event;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

class AcceptanceTests {

    private final static String TABLE_ID = "tableId";
    private KafkaEventProducer kafkaEventProducer;

    private WebClient webClient;

    @BeforeEach
    public void beforeEach() {
        webClient = WebClient.create("http://localhost:9585");
        kafkaEventProducer = new KafkaEventProducer("localhost:9092");
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

    @Test
    void reserveTableAndLeaveDoubleTry() {
        //given
        Mono<ExperimentResult> booleanFlux = reserveTableAndLeave()
                .delayUntil(experimentResult -> reserveTableAndLeave())
                .log()
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
        return requestTable(customerId, TABLE_ID)
                .delayElement(Duration.ofSeconds(delay))
                .flatMap(ignored -> payForTable(customerId, TABLE_ID))
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
        return requestTable(customerId, TABLE_ID)
                .delayElement(Duration.ofSeconds(delay))
                .flatMap(ignored -> fetchReservationStatus(customerId))
                .onErrorResume(WebClientResponseException.class, exception -> Mono.just(exception.getStatusCode().toString()))
                .map(status -> new ExperimentResult(customerId, status, "reserveTableAndLeave"));
    }

    private Mono<ExperimentResult> reserveTableAndPayLate() {
        String customerId = UUID.randomUUID().toString();
        long delay = 20L;
        return requestTable(customerId, TABLE_ID)
                .delayElement(Duration.ofSeconds(delay))
                .flatMap(ignored -> payForTable(customerId, TABLE_ID))
                .delayElement(Duration.ofSeconds(2))
                .flatMap(ignored -> fetchReservationStatus(customerId))
                .map(status -> new ExperimentResult(customerId, status, "reserveTableAndPayLate"));
    }

    private Mono<RecordMetadata> requestTable(String customerId, String TABLE_ID) {
        var event = new Event.CustomerEvent.CustomerRequestedTable(customerId, TABLE_ID);
        return kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC)
                .log("BMD::requestTable");
    }

    private Mono<RecordMetadata> payForTable(String customerId, String TABLE_ID) {
        var event = new Event.CustomerEvent.CustomerPaidForTable(customerId, TABLE_ID);
        return kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC);
    }

}
