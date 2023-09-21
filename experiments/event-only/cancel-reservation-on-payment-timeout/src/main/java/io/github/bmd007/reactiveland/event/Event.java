package io.github.bmd007.reactiveland.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.ZonedDateTime;
import java.util.UUID;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        property = "type",
        defaultImpl = Event.DefaultEvent.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Event.ReservationCancelledDueToPaymentTimeOut.class, name = "CustomerEvent"),
        @JsonSubTypes.Type(value = Event.DefaultEvent.class, name = "DefaultEvent")
})
public interface Event {

    default String type() {
        return this.getClass().getName();
    }

    default String key() {
        return UUID.randomUUID().toString();
    }

    default ZonedDateTime eventTime() {
        return ZonedDateTime.now();
    }

    record DefaultEvent() implements Event {
    }

    record ReservationCancelledDueToPaymentTimeOut(String customerId, String reservationId) implements Event {
        @Override
        public String key() {
            return reservationId;
        }

    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            property = "type",
            defaultImpl = CustomerEvent.DefaultCustomerEvent.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = CustomerEvent.CustomerRequestedTable.class, name = "CustomerRequestedTable"),
            @JsonSubTypes.Type(value = CustomerEvent.CustomerPaidForReservation.class, name = "CustomerPaidForReservation"),
    })
    interface CustomerEvent extends Event {
        String customerId();

        @Override
        default String key() {
            return customerId();
        }

        record DefaultCustomerEvent(String customerId) implements CustomerEvent {
        }

        record CustomerRequestedTable(String customerId, String reservationId) implements CustomerEvent {
        }

        record CustomerPaidForReservation(String customerId, String paymentId) implements CustomerEvent {
        }
    }

}
