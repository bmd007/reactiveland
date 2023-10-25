package io.github.bmd007.reactiveland.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import static io.github.bmd007.reactiveland.domain.TableReservation.Status.*;

@Value
@Builder
@With
@Jacksonized
public class TableReservation {

    public enum Status {
        AVAILABLE,
        RESERVED_AWAITING_PAYMENT,
        PAID_FOR,
    }

    String tableId;
    Status status;
    String customerId;

    public TableReservation paidFor() {
        if (isPayable()) {
            return withStatus(PAID_FOR);
        }
        throw new IllegalStateException("not payable");
    }

    public boolean isPayable() {
        return tableId != null && !isAvailable() && status.equals(RESERVED_AWAITING_PAYMENT);
    }

    public TableReservation awaitPayment(String customerId) {
        if (tableId == null) {
            throw new IllegalStateException("table id null");
        }
        if (isAvailable()) {
            return this.withStatus(RESERVED_AWAITING_PAYMENT).withCustomerId(customerId);
        }
        throw new IllegalStateException("not available");
    }

    public boolean isAvailable() {
        return customerId == null && status.equals(AVAILABLE);
    }

    public TableReservation makeAvailable() {
        return withCustomerId(null).withStatus(AVAILABLE);
    }

    public static TableReservation createTableReservation() {
        return TableReservation.builder().status(AVAILABLE).build();
    }

    public boolean isPaidFor() {
        return tableId != null && customerId != null && status != null && status.equals(Status.PAID_FOR);
    }
}
