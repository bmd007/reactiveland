package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.domain.ReservationAggregate;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerPaidForReservation;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerReservedTable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.github.bmd007.reactiveland.serialization.CustomSerdes.EVENT_CONSUMED;

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
        streamsBuilder.stream(Topics.CUSTOMER_EVENTS_TOPIC, EVENT_CONSUMED)
                .peek((key, value) -> log.info("one more event {}:{} ", key, value))
                .groupByKey()
                .windowedBy(timeWindows)
                .aggregate(ReservationAggregate::empty, (key, value, aggregate) ->
                        switch (value.type()) {
                            case "CustomerPaidForReservation" -> {
                                CustomerPaidForReservation customerPaidForReservation = (CustomerPaidForReservation) value;
                                log.info("CustomerReservedTable {} ", customerPaidForReservation);
                                yield aggregate.paidFor();
                            }
                            case "CustomerReservedTable" -> {
                                CustomerReservedTable customerReservedTable = (CustomerReservedTable) value;
                                log.info("customerReservedTable {} ", customerReservedTable);
                                yield aggregate.awaitPayment(customerReservedTable.reservationId(), customerReservedTable.customerId());
                            }
                            default -> aggregate;
                        }
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
