package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.configuration.StateStores;
import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.domain.TableReservation;
import io.github.bmd007.reactiveland.event.Event;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerPaidForTable;
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

    private TableReservation aggregation(String key, Event event, TableReservation currentTableReservation) {
        return switch (event) {
            case CustomerRequestedTable customerRequestedTable -> {
                if (currentTableReservation.getTableId() == null) {
                    yield currentTableReservation.withTableId(customerRequestedTable.tableId()).awaitPayment(key);
                }
                log.error("does not support parallel reservation per customer yet");
                yield null;
            }
            case CustomerPaidForTable ignored -> {
                try {
                    yield currentTableReservation.paidFor();
                } catch (Exception e) {
                    log.error("error when setting the table reservation {} status to paid for", currentTableReservation, e);
                    yield currentTableReservation;
                }
            }
            default -> currentTableReservation;
        };
    }

    @PostConstruct
    public void configureStores() {
        var suppressedConfig = new StrictBufferConfigImpl().withMaxRecords(2).emitEarlyWhenFull();
        TimeWindows timeWindows = TimeWindows.ofSizeAndGrace(Duration.ofSeconds(15), Duration.ofSeconds(1));
        streamsBuilder.stream(Topics.CUSTOMER_EVENTS_TOPIC, EVENT_CONSUMED)
                .groupByKey(Grouped.with(Serdes.String(), EVENT_JSON_SERDE))
                .windowedBy(timeWindows)
                .aggregate(TableReservation::createTableReservation, this::aggregation, RESERVATION_LOCAL_KTABLE_MATERIALIZED)
//                .suppress(Suppressed.untilWindowCloses(suppressedConfig))
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(15), suppressedConfig))
                .toStream()
                .foreach((key, tableReservation) -> {
                    log.info("BMD:: \n final {} ", tableReservation);
                    // we can produce events into other topic to update the actual state machine of orders
                    // todo: we don't get the trigger for time out.  We end up here when paid, either on time or late. But not when timedout.
                    // the suppress messes things up somehow.
                    // also apparently the window sometimes gets closed to early
                    LocalTime startTime = LocalTime.ofInstant(key.window().startTime(), ZoneId.of("Europe/Stockholm"));
                    LocalTime endTime = LocalTime.ofInstant(key.window().endTime(), ZoneId.of("Europe/Stockholm"));
                    log.info("window length {}:{}", startTime, endTime);
                });
    }

    //**
    // // Define a TimeWindows window with a size of 10 seconds and a grace period of 5 seconds
    //TimeWindows window = TimeWindows.of(Duration.ofSeconds(10)).grace(Duration.ofSeconds(5));
    //
    //// Join the request stream with the response stream using the leftJoin method
    //KStream<String, Event> events = requests.leftJoin(
    //responses,
    //(request, response) -> new Event(request, response), // join function
    //window,
    //Joined.with(Serdes.String(), new RequestSerde(), new ResponseSerde()) // serdes for key and value
    //);
    //
    //// Transform the joined record into an event based on the presence or absence of the response value
    //events = events.mapValues((key, event) -> {
    //if (event.getResponse() == null) {
    //// No response was found within the time window, emit a timeout event
    //event.setType("timeout");
    //event.setMessage("No response received for request " + event.getRequest().getRequestId());
    //} else {
    //// A response was found within the time window, check if it was before or after the window end
    //long requestTime = event.getRequest().getTimestamp();
    //long responseTime = event.getResponse().getTimestamp();
    //long windowEnd = requestTime + window.sizeMs();
    //if (responseTime <= windowEnd) {
    //// The response was received before the window end, emit a success event
    //event.setType("success");
    //event.setMessage("Response received for request " + event.getRequest().getRequestId());
    //} else {
    //// The response was received after the window end, emit an expiry event
    //event.setType("expiry");
    //event.setMessage("Response received too late for request " + event.getRequest().getRequestId());
    //}
    //}
    //return event;
    //});
    //
    // *//
}
