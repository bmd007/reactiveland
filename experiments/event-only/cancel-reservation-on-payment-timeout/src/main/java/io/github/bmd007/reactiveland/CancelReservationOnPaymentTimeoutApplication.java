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

import java.time.Duration;

@RestController
@SpringBootApplication
public class CancelReservationOnPaymentTimeoutApplication {

	public static void main(String[] args) {
		SpringApplication.run(CancelReservationOnPaymentTimeoutApplication.class, args);
	}

	@Autowired
	private KafkaEventProducer kafkaEventProducer;

	@GetMapping
	public Flux<String> publish(){
		String customerId1 = "customerId1";
		String customerId2 = "customerId2";
		return Flux.range(0, 10)
				.flatMap(integer -> integer % 2 == 0 ? Mono.just(customerId2) : Mono.delay(Duration.ofSeconds(1)).just(customerId1))
				.map(customerId -> new Event.CustomerEvent.CustomerReservedTable(customerId, "reserve-id"))
				.flatMap(kafkaEventProducer::produceCustomerEvent)
				.map(RecordMetadata::topic);
	}

	@EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
	public void contextRefreshedEvent() {
	}
}
