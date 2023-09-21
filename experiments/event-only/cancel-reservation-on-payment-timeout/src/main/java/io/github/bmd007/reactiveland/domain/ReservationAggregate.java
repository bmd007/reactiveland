package io.github.bmd007.reactiveland.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import static io.github.bmd007.reactiveland.domain.ReservationAggregate.ReservationStatus.JUST_CREATED;

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

    public static ReservationAggregate createEmpty(){
        return ReservationAggregate.builder().status(JUST_CREATED).build();
    }
}
