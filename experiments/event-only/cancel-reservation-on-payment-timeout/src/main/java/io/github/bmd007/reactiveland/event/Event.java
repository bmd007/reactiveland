package io.github.bmd007.reactiveland.event;

import java.time.ZonedDateTime;

public interface Event {

    default String eventName() {
        return this.getClass().getSimpleName();
    }

    String key();

    default ZonedDateTime eventTime() {
        return ZonedDateTime.now();
    }

    interface CustomerEvent extends Event {
        String customerId();

        default String key() {
            return customerId();
        }

        record CustomerReservedTable(String customerId, String reservationId) implements CustomerEvent {
        }

        record CustomerPaidForReservation(String customerId, String paymentId) implements CustomerEvent {
        }
    }

    record ReservationCancelledDueToPaymentTimeOut(String customerId, String reservationId) implements Event {
        @Override
        public String key() {
            return reservationId;
        }
    }
}
