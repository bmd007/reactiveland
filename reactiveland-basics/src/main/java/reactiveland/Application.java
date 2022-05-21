package reactiveland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        Schedulers.enableMetrics();
        SpringApplication.run(Application.class, args);
    }

}
