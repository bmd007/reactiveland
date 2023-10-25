package reactiveland.show.season2.episode5;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;


@ActiveProfiles("test")
@ExtendWith({SpringExtension.class})
@DirtiesContext
@SpringBootTest
@Testcontainers
@AutoConfigureWebTestClient
class MusicApplicationTest {

    @Container
    public static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>("postgres:14.3")
            .withDatabaseName("reactiveland-db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("postgres.port", () -> postgresDB.getFirstMappedPort());//this is jdbc url
        registry.add("postgres.url", postgresDB::getJdbcUrl);//this is jdbc url
        registry.add("postgres.username", postgresDB::getUsername);
        registry.add("postgres.password", postgresDB::getPassword);
        registry.add("postgres.db", postgresDB::getDatabaseName);

        Flyway flyway = Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(postgresDB.getJdbcUrl(), postgresDB.getUsername(), postgresDB.getPassword())
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }

    @Autowired
    WebTestClient webTestClient;

    @Test
    void createAMusicThenFindItAmongCreatedMusics() {
        //given
        //todo
        //when
        //todo
        //then
        //todo
    }

}
