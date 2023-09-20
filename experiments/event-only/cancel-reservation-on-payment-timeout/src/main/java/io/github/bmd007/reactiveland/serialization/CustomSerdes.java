package io.github.bmd007.reactiveland.serialization;

import io.github.bmd007.reactiveland.event.Event;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

public class CustomSerdes {
    public static final JsonSerde<Event.CustomerEvent> CUSTOMER_EVENT_JSON_SERDE =
            new JsonSerde<>(Event.CustomerEvent.class);
    public static final JsonSerde<Event.CustomerEvent.CustomerReservedTable> CUSTOMER_RESERVED_TABLE_JSON_SERDE =
            new JsonSerde<>(Event.CustomerEvent.CustomerReservedTable.class);
    public static final JsonSerde<Event.CustomerEvent.CustomerPaidForReservation> CUSTOMER_PAID_FOR_RESERVATION_JSON_SERDE =
            new JsonSerde<>(Event.CustomerEvent.CustomerPaidForReservation.class);
    public static final JsonSerde<Event.ReservationCancelledDueToPaymentTimeOut> RESERVATION_CANCELLED_DUE_TO_PAYMENT_TIME_OUT_JSON_SERDE =
            new JsonSerde<>(Event.ReservationCancelledDueToPaymentTimeOut.class);

    public static final Consumed<String, Event.CustomerEvent.CustomerReservedTable> CUSTOMER_RESERVED_TABLE_CONSUMED =
            Consumed.with(Serdes.String(), CUSTOMER_RESERVED_TABLE_JSON_SERDE);

    public static final Produced<String, Event.CustomerEvent.CustomerReservedTable> CUSTOMER_RESERVED_TABLE_PRODUCED =
            Produced.with(Serdes.String(), CUSTOMER_RESERVED_TABLE_JSON_SERDE);
    public static final Consumed<String, Event.CustomerEvent.CustomerPaidForReservation> CUSTOMER_PAID_FOR_RESERVATION_CONSUMED =
            Consumed.with(Serdes.String(), CUSTOMER_PAID_FOR_RESERVATION_JSON_SERDE);

    public static final Produced<String, Event.CustomerEvent.CustomerPaidForReservation> CUSTOMER_PAID_FOR_RESERVATION_PRODUCED =
            Produced.with(Serdes.String(), CUSTOMER_PAID_FOR_RESERVATION_JSON_SERDE);

    public static final Consumed<String, Event.CustomerEvent.ReservationCancelledDueToPaymentTimeOut> CANCELLED_DUE_TO_PAYMENT_TIME_OUT_CONSUMED =
            Consumed.with(Serdes.String(), RESERVATION_CANCELLED_DUE_TO_PAYMENT_TIME_OUT_JSON_SERDE);

    public static final Produced<String, Event.CustomerEvent.ReservationCancelledDueToPaymentTimeOut> CANCELLED_DUE_TO_PAYMENT_TIME_OUT_PRODUCED =
            Produced.with(Serdes.String(), RESERVATION_CANCELLED_DUE_TO_PAYMENT_TIME_OUT_JSON_SERDE);

}
