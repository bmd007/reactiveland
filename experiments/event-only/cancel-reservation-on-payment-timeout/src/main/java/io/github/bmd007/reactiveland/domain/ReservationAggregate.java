package io.github.bmd007.reactiveland.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
public class ReservationAggregate {

    public enum ReservationStatus {
        AWAITING_CUSTOMER_PAYMENT,
        RESERVED_FOR_CUSTOMER,
    }

    String reservationId;
    ReservationStatus status;
    String customerId;

    public ReservationAggregate paidFor(){
        if (this.status.equals(ReservationStatus.RESERVED_FOR_CUSTOMER)){
            throw new IllegalStateException("already paid for");
        }
        return this.withStatus(ReservationStatus.RESERVED_FOR_CUSTOMER);
    }

    public ReservationAggregate awaitPayment(String reservationId, String customerId){
        if (this.status.equals(ReservationStatus.AWAITING_CUSTOMER_PAYMENT)){
            throw new IllegalStateException("already waiting for customer payment");
        }
        return this.withStatus(ReservationStatus.AWAITING_CUSTOMER_PAYMENT);
    }

    public static ReservationAggregate empty(){
        return ReservationAggregate.builder().build();
    }

}
