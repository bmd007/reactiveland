package reactiveland.experiment.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

import java.sql.SQLException;

@Slf4j
@SpringBootApplication
public class WebfluxApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebfluxApplication.class, args);
    }

    private org.h2.tools.Server webServer;
    private org.h2.tools.Server server;

    @EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
    public void setupH2Console() {
        try {
            this.webServer = org.h2.tools.Server.createWebServer("-webAllowOthers", "-webPort", "8084").start();
            this.server = org.h2.tools.Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9097").start();
        } catch (SQLException throwable) {
            log.error("", throwable);
        }
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void stop() {
        this.webServer.stop();
        this.server.stop();
    }
}
