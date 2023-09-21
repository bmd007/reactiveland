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

import static io.github.bmd007.reactiveland.serialization.CustomSerdes.*;

@Component
public class KafkaEventProducer {

    private final KafkaProducer<String, Event> eventKafkaProducer;

    public KafkaEventProducer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        var providerConfig = new Properties();
        providerConfig.put("bootstrap.servers", bootstrapServers);
        eventKafkaProducer = new KafkaProducer<>(providerConfig, new StringSerializer(), EVENT_JSON_SERDE.serializer());
    }

    public Mono<RecordMetadata> produceEvent(Event event, String topic) {
        var record = new ProducerRecord<>(topic, event.key(), event);
        return Mono.fromCallable(() -> eventKafkaProducer.send(record).get());
    }
}
