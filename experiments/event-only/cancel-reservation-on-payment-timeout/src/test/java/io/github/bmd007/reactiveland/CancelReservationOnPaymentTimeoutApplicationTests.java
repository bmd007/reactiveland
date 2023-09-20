package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.Topics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {Topics.RESERVATION_EVENTS_TOPIC, Topics.CUSTOMER_EVENTS_TOPIC})
class CancelReservationOnPaymentTimeoutApplicationTests {

	@Test
	void contextLoads() {
	}

}
