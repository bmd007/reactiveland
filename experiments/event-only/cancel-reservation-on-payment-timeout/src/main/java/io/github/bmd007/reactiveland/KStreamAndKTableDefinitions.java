package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.configuration.StateStores;
import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.domain.TableReservation;
import io.github.bmd007.reactiveland.event.Event;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerPaidForReservation;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerRequestedTable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.internals.suppress.StrictBufferConfigImpl;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.github.bmd007.reactiveland.serialization.CustomSerdes.*;

@Slf4j
@Configuration
public class KStreamAndKTableDefinitions {

    private static final Materialized<String, TableReservation, WindowStore<Bytes, byte[]>> RESERVATION_LOCAL_KTABLE_MATERIALIZED =
            Materialized.<String, TableReservation, WindowStore<Bytes, byte[]>>as(StateStores.RESERVATION_STATUS_IN_MEMORY_STATE_STORE)
                    .withStoreType(Materialized.StoreType.IN_MEMORY)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(RESERVATION_AGGREGATE_JSON_SERDE);

    private final StreamsBuilder streamsBuilder;
    private final KafkaEventProducer kafkaEventProducer;

    public KStreamAndKTableDefinitions(StreamsBuilder streamsBuilder, KafkaEventProducer kafkaEventProducer) {
        this.streamsBuilder = streamsBuilder;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    private static TableReservation aggregation(String key, Event value, TableReservation aggregate) {
        return switch (value) {
            case CustomerRequestedTable customerRequestedTable -> {
                TableReservation tableReservation = aggregate.withTableId(customerRequestedTable.tableId()).awaitPayment(key);
//                log.info("awaiting payment {}", tableReservation);
                yield tableReservation;
            }
            case CustomerPaidForReservation ignored -> {
                try {
                    yield aggregate.paidFor();
                } catch (Exception e) {
                    log.error("error when setting the table paid for {}", aggregate, e);
                    yield aggregate;
                }
            }
            default -> TableReservation.createTable();
        };
    }

    @PostConstruct
    public void configureStores() {
        TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10));
        streamsBuilder.stream(Topics.CUSTOMER_EVENTS_TOPIC, EVENT_CONSUMED)
//                .peek((key, value) -> log.info("new event {}", value))
                .groupByKey(Grouped.with(Serdes.String(), EVENT_JSON_SERDE))
                .windowedBy(timeWindows)
                .aggregate(TableReservation::createTable, KStreamAndKTableDefinitions::aggregation, RESERVATION_LOCAL_KTABLE_MATERIALIZED)
                .suppress(Suppressed.untilWindowCloses(new StrictBufferConfigImpl()))
                .toStream()
                //maybe add a stable (not windowed) ktable for finalized reservations here
                .foreach((key, tableReservation) -> {
                    LocalTime startTime = ZonedDateTime.ofInstant(key.window().startTime(), ZoneId.systemDefault()).toLocalTime();
                    LocalTime endTime = ZonedDateTime.ofInstant(key.window().endTime(), ZoneId.systemDefault()).toLocalTime();
                    log.info("{}:{} --- {}:{}", key.key(), tableReservation, startTime.getSecond(), endTime.getSecond());
                });
    }
}
