package io.github.bmd007.reactiveland;

import static io.github.bmd007.reactiveland.domain.TableReservation.Status.PAID_FOR;

public record ExperimentResult(String customerId, String resultStatus, String methodName, boolean wasSuccessful) {

    public ExperimentResult(String customerId, String resultStatus, String methodName) {
        this(customerId, resultStatus, methodName, wasSuccessful(methodName, resultStatus));
    }

    private static boolean wasSuccessful(String methodName, String resultStatus) {
        return switch (methodName) {
            case "reserveAndPayForTable" -> resultStatus.equals(PAID_FOR.name());
            case "reserveTableAndLeave" -> resultStatus.equals("404 NOT_FOUND");
            case "reserveTableAndPayLate" -> resultStatus.equals("404 NOT_FOUND");
            default -> throw new IllegalStateException("Unexpected value: " + methodName);
        };
    }
}