package io.github.bmd007.reactiveland.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@With
@Jacksonized
public class ReservationAggregate {

    public enum ReservationStatus {
        AWAITING_CUSTOMER_PAYMENT,
        RESERVED_FOR_CUSTOMER,
    }

    String reservationId;
    ReservationStatus status;
    String customerId;
}
