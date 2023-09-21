package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.StateStores;
import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.domain.ReservationAggregate;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerPaidForReservation;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerRequestedTable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.github.bmd007.reactiveland.domain.ReservationAggregate.ReservationStatus.*;
import static io.github.bmd007.reactiveland.serialization.CustomSerdes.*;

@Slf4j
@Configuration
public class KStreamAndKTableDefinitions {

    private static final Materialized<String, ReservationAggregate, WindowStore<Bytes, byte[]>> RESERVATION_LOCAL_KTABLE_MATERIALIZED =
            Materialized.<String, ReservationAggregate, WindowStore<Bytes, byte[]>>as(StateStores.RESERVATION_STATUS_IN_MEMORY_STATE_STORE)
                    .withStoreType(Materialized.StoreType.IN_MEMORY)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(RESERVATION_AGGREGATE_JSON_SERDE);


    private final StreamsBuilder streamsBuilder;

    public KStreamAndKTableDefinitions(StreamsBuilder streamsBuilder) {
        this.streamsBuilder = streamsBuilder;
    }

    @PostConstruct
    public void configureStores() {
        TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(20));
        streamsBuilder.stream(Topics.CUSTOMER_EVENTS_TOPIC, EVENT_CONSUMED)
                .groupByKey(Grouped.with(Serdes.String(), EVENT_JSON_SERDE))
                .windowedBy(timeWindows)
                .aggregate(ReservationAggregate::createEmpty,
                        (key, value, aggregate) -> switch (value) {
                            case CustomerRequestedTable customerRequestedTable -> aggregate.withStatus(AWAITING_PAYMENT)
                                    .withCustomerId(key)
                                    .withReservationId(customerRequestedTable.reservationId());
                            case CustomerPaidForReservation ignored -> aggregate.withStatus(FINALIZED);
                            default -> ReservationAggregate.createEmpty();
                        },
                        RESERVATION_LOCAL_KTABLE_MATERIALIZED
                )
                .toStream()
                .foreach((key, value) -> {
                    // Perform actions based on processing time window closure
                    // This gets executed when the window closes based on processing time
                    LocalTime startTime = ZonedDateTime.ofInstant(key.window().startTime(), ZoneId.systemDefault()).toLocalTime();
                    LocalTime endTime = ZonedDateTime.ofInstant(key.window().endTime(), ZoneId.systemDefault()).toLocalTime();
                    log.info("{}:{} --- {}:{}", key.key(), value, startTime.getSecond(), endTime.getSecond());
                    // Implement your actions here
                });
    }
}
