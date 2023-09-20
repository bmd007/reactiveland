package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;

import java.util.UUID;

@SpringBootApplication
public class CancelReservationOnPaymentTimeoutApplication {

	public static void main(String[] args) {
		SpringApplication.run(CancelReservationOnPaymentTimeoutApplication.class, args);
	}

	@Autowired
	private KafkaEventProducer kafkaEventProducer;

	@EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
	public void contextRefreshedEvent() {
		UUID customerId1 = UUID.randomUUID();
		UUID customerId2 = UUID.randomUUID();
		Flux.range(0, 10)
				.map(integer -> integer % 2 == 0 ? customerId1 : customerId2)
				.map(UUID::toString)
				.map(uuid -> new Event.CustomerEvent.CustomerReservedTable(uuid, "reserve-id"))
				.flatMap(kafkaEventProducer::produceCustomerEvent)
				.log()
				.subscribe();
	}
}
