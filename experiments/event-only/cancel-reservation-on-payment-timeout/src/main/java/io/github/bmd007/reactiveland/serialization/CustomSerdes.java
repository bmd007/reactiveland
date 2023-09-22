package io.github.bmd007.reactiveland.serialization;

import io.github.bmd007.reactiveland.domain.TableReservation;
import io.github.bmd007.reactiveland.event.Event;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.kafka.support.serializer.JsonSerde;

public class CustomSerdes {
    public static final JsonSerde<TableReservation> RESERVATION_AGGREGATE_JSON_SERDE =
            new JsonSerde<>(TableReservation.class);
    public static final JsonSerde<Event> EVENT_JSON_SERDE =
            new JsonSerde<>(Event.class);
    public static final JsonSerde<Event.CustomerEvent> CUSTOMER_EVENT_JSON_SERDE =
            new JsonSerde<>(Event.CustomerEvent.class);
    public static final JsonSerde<Event.CustomerEvent.CustomerRequestedTable> CUSTOMER_RESERVED_TABLE_JSON_SERDE =
            new JsonSerde<>(Event.CustomerEvent.CustomerRequestedTable.class);
    public static final JsonSerde<Event.CustomerEvent.CustomerPaidForReservation> CUSTOMER_PAID_FOR_RESERVATION_JSON_SERDE =
            new JsonSerde<>(Event.CustomerEvent.CustomerPaidForReservation.class);

    public static final Consumed<String, Event> EVENT_CONSUMED =
            Consumed.with(Serdes.String(), EVENT_JSON_SERDE);

    public static final Produced<String, Event> EVENT_PRODUCED =
            Produced.with(Serdes.String(), EVENT_JSON_SERDE);

    public static final Consumed<String, Event.CustomerEvent> CUSTOMER_EVENT_CONSUMED =
            Consumed.with(Serdes.String(), CUSTOMER_EVENT_JSON_SERDE);

    public static final Produced<String, Event.CustomerEvent> CUSTOMER_EVENT_PRODUCED =
            Produced.with(Serdes.String(), CUSTOMER_EVENT_JSON_SERDE);

    public static final Consumed<String, Event.CustomerEvent.CustomerRequestedTable> CUSTOMER_RESERVED_TABLE_CONSUMED =
            Consumed.with(Serdes.String(), CUSTOMER_RESERVED_TABLE_JSON_SERDE);

    public static final Produced<String, Event.CustomerEvent.CustomerRequestedTable> CUSTOMER_RESERVED_TABLE_PRODUCED =
            Produced.with(Serdes.String(), CUSTOMER_RESERVED_TABLE_JSON_SERDE);
    public static final Consumed<String, Event.CustomerEvent.CustomerPaidForReservation> CUSTOMER_PAID_FOR_RESERVATION_CONSUMED =
            Consumed.with(Serdes.String(), CUSTOMER_PAID_FOR_RESERVATION_JSON_SERDE);

    public static final Produced<String, Event.CustomerEvent.CustomerPaidForReservation> CUSTOMER_PAID_FOR_RESERVATION_PRODUCED =
            Produced.with(Serdes.String(), CUSTOMER_PAID_FOR_RESERVATION_JSON_SERDE);

}
