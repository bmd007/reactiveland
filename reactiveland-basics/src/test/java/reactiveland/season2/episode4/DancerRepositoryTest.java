package reactiveland.season2.episode4;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@ActiveProfiles("test")
@ExtendWith({SpringExtension.class})
@DirtiesContext
//@DataR2dbcTest
@SpringBootTest
@Testcontainers
class DancerRepositoryTest {

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
    DancerRepository dancerRepository;

    @Test
    void assertANewlyInsertedDancerInDb() {
        //given
        var dancer = Dancer.newDancer(Dancer.DanceType.FREE_STYLE);
        //when
        Mono<Dancer> dancerMono = Mono.just(dancer).flatMap(dancerRepository::save);
        //todo
     }

     @Test
    void assertAChangeInDancerLastDancedAt(){
        //given
         //todo
         //when
         //todo
         //then
         //todo
     }

}
