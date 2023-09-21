package io.github.bmd007.reactiveland.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import static io.github.bmd007.reactiveland.domain.ReservationAggregate.ReservationStatus.*;

@Value
@Builder
@With
@Jacksonized
public class ReservationAggregate {

    public enum ReservationStatus {
        JUST_CREATED,
        AWAITING_PAYMENT,
        FINALIZED,
    }

    String reservationId;
    ReservationStatus status;
    String customerId;

    public ReservationAggregate finalizeReservation() {
        if (!isEmpty() && !status.equals(FINALIZED) && status.equals(AWAITING_PAYMENT)) {
            return withStatus(FINALIZED);
        }
        throw new IllegalStateException("already finalized or not initialized yet");
    }

    public ReservationAggregate awaitPayment(String customerId, String reservationId) {
        if (isEmpty()) {
            return this.withStatus(AWAITING_PAYMENT).withCustomerId(customerId).withReservationId(reservationId);
        }
        throw new IllegalStateException("already initialized");
    }

    public boolean isEmpty() {
        return customerId == null || reservationId == null || status == null || status.equals(JUST_CREATED);
    }

    public static ReservationAggregate createEmpty() {
        return ReservationAggregate.builder().status(JUST_CREATED).build();
    }
}
