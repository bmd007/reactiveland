package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerPaidForReservation;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerRequestedTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@RestController
@SpringBootApplication
public class CancelReservationOnPaymentTimeoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(CancelReservationOnPaymentTimeoutApplication.class, args);
    }

    @Autowired
    private KafkaEventProducer kafkaEventProducer;


    @GetMapping("/customer1")
    public Mono<String> customer1ReserveAndPayInTime() {
        String customerId1 = UUID.randomUUID().toString();
        return kafkaEventProducer.produceEvent(new CustomerRequestedTable(customerId1, "table1"), Topics.CUSTOMER_EVENTS_TOPIC)
                .delayElement(Duration.ofSeconds(5))
                .map(ignored -> new CustomerPaidForReservation(customerId1, "payment1"))
                .flatMap(event -> kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC))
                .map(RecordMetadata::topic);
    }

    @GetMapping("/customer2")
    public Mono<String> customer2ReserveAndPayInTime() {
        String customerId2 = UUID.randomUUID().toString();
        return kafkaEventProducer.produceEvent(new CustomerRequestedTable(customerId2, "table2"), Topics.CUSTOMER_EVENTS_TOPIC)
                .delayElement(Duration.ofSeconds(10))
                .map(ignored -> new CustomerPaidForReservation(customerId2, "payment2"))
                .flatMap(event -> kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC))
                .map(RecordMetadata::topic);
    }
}
