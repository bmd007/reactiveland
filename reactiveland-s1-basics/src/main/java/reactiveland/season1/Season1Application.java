package reactiveland.season1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class Season1Application {

	public static void main(String[] args) {
		Schedulers.enableMetrics();
		SpringApplication.run(Season1Application.class, args);
	}

}
