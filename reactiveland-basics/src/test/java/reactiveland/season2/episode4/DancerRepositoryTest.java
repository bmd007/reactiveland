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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ActiveProfiles("test")
@ExtendWith({SpringExtension.class})
@DirtiesContext
@DataR2dbcTest
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
        Mono<Dancer> savedDancerMono = dancerRepository.save(dancer);
        //then
        StepVerifier.create(savedDancerMono)
                .expectNext(dancer)
                .expectComplete()
                .verify();
     }

     @Test
    void assertAChangeInDancerLastDancedAt(){
        //given
         var dancer = Dancer.newDancer(Dancer.DanceType.FREE_STYLE);
         //when
         Mono<Dancer> updatedDancerMono = dancerRepository.save(dancer)
                 .map(Dancer::dance)
                 .flatMap(dancerRepository::save);
         //then
         StepVerifier.create(updatedDancerMono)
                 .assertNext(updatedDancer -> assertTrue(updatedDancer.lastDancedAt().isAfter(LocalDate.of(2001, 1, 1).atStartOfDay())))
                 .expectComplete()
                 .verify();
     }

}
