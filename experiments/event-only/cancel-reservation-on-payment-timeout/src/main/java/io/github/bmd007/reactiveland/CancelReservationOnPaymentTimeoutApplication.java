package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.configuration.Topics;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerPaidForReservation;
import io.github.bmd007.reactiveland.event.Event.CustomerEvent.CustomerRequestedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@SpringBootApplication
public class CancelReservationOnPaymentTimeoutApplication {

	public static void main(String[] args) {
		SpringApplication.run(CancelReservationOnPaymentTimeoutApplication.class, args);
	}

	@Autowired
	private KafkaEventProducer kafkaEventProducer;

	@GetMapping
	public Mono<Long> publish() throws InterruptedException {
		String customerId1 = "customerId1";
		String customerId2 = "customerId2";

		kafkaEventProducer.produceEvent(new CustomerRequestedTable(customerId1, "reserve-id1"), Topics.CUSTOMER_EVENTS_TOPIC).block();
		kafkaEventProducer.produceEvent(new CustomerRequestedTable(customerId2, "reserve-id2"), Topics.CUSTOMER_EVENTS_TOPIC).block();
		Thread.sleep(7000);
		kafkaEventProducer.produceEvent(new CustomerPaidForReservation(customerId1, "payment-id1"), Topics.CUSTOMER_EVENTS_TOPIC).block();
		Thread.sleep(27000);
		kafkaEventProducer.produceEvent(new CustomerPaidForReservation(customerId2, "payment-id2"), Topics.CUSTOMER_EVENTS_TOPIC).block();
		return Flux.range(0, 10)
//				.delayUntil(integer -> integer % 2 == 0 ? Mono.just("ignore") : Mono.delay(Duration.ofSeconds(1)))
//				.map(integer -> integer % 2 == 0 ? customerId2 : customerId1)
//				.map(customerId -> new CustomerRequestedTable(customerId, "reserve-id"))
//				.flatMap(event -> kafkaEventProducer.produceEvent(event, Topics.CUSTOMER_EVENTS_TOPIC))
//				.zipWith(kafkaEventProducer.produceEvent(new CustomerPaidForReservation(customerId1, "payment-id"), Topics.CUSTOMER_EVENTS_TOPIC).cache().repeat())
				.count();
	}

	@EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
	public void contextRefreshedEvent() {
	}
}
