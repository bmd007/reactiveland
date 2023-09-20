package io.github.bmd007.reactiveland;

import io.github.bmd007.reactiveland.configuration.KafkaEventProducer;
import io.github.bmd007.reactiveland.event.Event;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@SpringBootApplication
public class CancelReservationOnPaymentTimeoutApplication {

	public static void main(String[] args) {
		SpringApplication.run(CancelReservationOnPaymentTimeoutApplication.class, args);
	}

	@Autowired
	private KafkaEventProducer kafkaEventProducer;

	@GetMapping
	public Mono<List<String>> publish(){
		UUID customerId1 = UUID.randomUUID();
		UUID customerId2 = UUID.randomUUID();
		return Flux.range(0, 10)
				.map(integer -> integer % 2 == 0 ? customerId1 : customerId2)
				.map(UUID::toString)
				.map(uuid -> new Event.CustomerEvent.CustomerReservedTable(uuid, "reserve-id"))
				.flatMap(kafkaEventProducer::produceCustomerEvent)
				.map(RecordMetadata::topic)
				.collectList();
	}

	@EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
	public void contextRefreshedEvent() {
	}
}
