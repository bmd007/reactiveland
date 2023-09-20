package io.github.bmd007.reactiveland.configuration;

import io.github.bmd007.reactiveland.event.Event;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Properties;

import static io.github.bmd007.reactiveland.serialization.CustomSerdes.CUSTOMER_EVENT_JSON_SERDE;
import static io.github.bmd007.reactiveland.serialization.CustomSerdes.RESERVATION_CANCELLED_DUE_TO_PAYMENT_TIME_OUT_JSON_SERDE;

@Component
public class KafkaEventProducer {

    private final KafkaProducer<String, Event.CustomerEvent> customerEventKafkaProducer;
    private final KafkaProducer<String, Event.ReservationCancelledDueToPaymentTimeOut> reservationCancelledOutKafkaProducer;

    public KafkaEventProducer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        var providerConfig = new Properties();
        providerConfig.put("bootstrap.servers", bootstrapServers);
        customerEventKafkaProducer = new KafkaProducer<>(providerConfig, new StringSerializer(), CUSTOMER_EVENT_JSON_SERDE.serializer());
        reservationCancelledOutKafkaProducer = new KafkaProducer<>(providerConfig, new StringSerializer(),
                RESERVATION_CANCELLED_DUE_TO_PAYMENT_TIME_OUT_JSON_SERDE.serializer());
    }

    public Mono<RecordMetadata> produceCustomerEvent(Event.CustomerEvent customerEvent) {
        var record = new ProducerRecord<>(Topics.CUSTOMER_EVENTS_TOPIC, customerEvent.key(), customerEvent);
        return Mono.fromCallable(() -> customerEventKafkaProducer.send(record).get());

    }

    public Mono<RecordMetadata> produceReservationCancelledEvent(Event.ReservationCancelledDueToPaymentTimeOut reservationCancelled) {
        var record = new ProducerRecord<>(Topics.RESERVATION_EVENTS_TOPIC, reservationCancelled.key(), reservationCancelled);
        return Mono.fromCallable(() -> reservationCancelledOutKafkaProducer.send(record).get());
    }
}
