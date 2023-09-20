package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.Topics;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static io.github.bmd007.reactiveland.serialization.CustomSerdes.*;

@Slf4j
@Configuration
public class KStreamAndKTableDefinitions {

//    private static final Materialized<String, WonderSeeker, KeyValueStore<Bytes, byte[]>> WONDER_SEEKER_LOCAL_STATE_KTABLE = Materialized
//            .<String, WonderSeeker>as(Stores.inMemoryKeyValueStore(WONDER_SEEKER_IN_MEMORY_STATE_STORE))
//            .withKeySerde(Serdes.String())
//            .withValueSerde(WONDER_SEEKER_JSON_SERDE);

    private final StreamsBuilder streamsBuilder;

    public KStreamAndKTableDefinitions(StreamsBuilder streamsBuilder) {
        this.streamsBuilder = streamsBuilder;
    }

    @PostConstruct
    public void configureStores() {
        TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(2));
        streamsBuilder.stream(Topics.CUSTOMER_EVENTS_TOPIC, CUSTOMER_RESERVED_TABLE_CONSUMED)
                .groupByKey()
                .windowedBy(timeWindows)
                .count()
                .toStream()
                .foreach((key, value) -> {
                    // Perform actions based on processing time window closure
                    // This gets executed when the window closes based on processing time
                    log.info("at {} windows {} closed with value {}", key.window().end(), key.key(), value);
                    // Implement your actions here
                });
    }
}
