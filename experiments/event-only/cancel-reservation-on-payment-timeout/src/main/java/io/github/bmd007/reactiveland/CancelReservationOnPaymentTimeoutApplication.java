package io.github.bmd007.reactiveland;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@SpringBootApplication
public class CancelReservationOnPaymentTimeoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(CancelReservationOnPaymentTimeoutApplication.class, args);
    }
}
