package io.github.bmd007.reactiveland.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        property = "type",
        defaultImpl = Event.DefaultEvent.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Event.CustomerEvent.class, name = "CustomerEvent"),
        @JsonSubTypes.Type(value = Event.DefaultEvent.class, name = "DefaultEvent")
})
public interface Event {

    default String type() {
        return this.getClass().getName();
    }

    default String key() {
        return UUID.randomUUID().toString();
    }

    default Instant timestamp() {
        return ZonedDateTime.now().minusMonths(1L).toInstant();
    }

    record DefaultEvent() implements Event {
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            property = "type",
            defaultImpl = CustomerEvent.DefaultCustomerEvent.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = CustomerEvent.CustomerRequestedTable.class, name = "CustomerRequestedTable"),
            @JsonSubTypes.Type(value = CustomerEvent.CustomerPaidForTable.class, name = "CustomerPaidForReservation"),
    })
    interface CustomerEvent extends Event {
        String customerId();

        @Override
        default String key() {
            return customerId();
        }

        record DefaultCustomerEvent(String customerId) implements CustomerEvent {
        }

        record CustomerRequestedTable(String customerId, @Nonnull String tableId) implements CustomerEvent {
        }

        record CustomerPaidForTable(String customerId, @Nonnull String tableId) implements CustomerEvent {
        }
    }

}
