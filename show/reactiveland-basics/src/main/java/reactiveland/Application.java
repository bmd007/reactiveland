package reactiveland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        Schedulers.enableMetrics();
        SpringApplication app = new SpringApplication(Application.class);
        app.setWebApplicationType(WebApplicationType.REACTIVE);
        app.run(args);
    }

}
