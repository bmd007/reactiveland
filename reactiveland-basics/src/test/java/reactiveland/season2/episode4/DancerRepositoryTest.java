package reactiveland.season2.episode4;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@ExtendWith({SpringExtension.class})
@DirtiesContext
@DataR2dbcTest(includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {DancerRepository.class})})
class DancerRepositoryTest {

    @Autowired
    DancerRepository dancerRepository;

    @Autowired
    R2dbcEntityOperations r2dbcOperations;

    @Test
    void assertANewlyInsertedDancerInDb() {
        //given
        var dancer = Dancer.newDancer(Dancer.DanceType.FREE_STYLE);
        //when
        Mono<Dancer> dancerMono = dancerRepository.save(dancer);
        //
        StepVerifier.create(dancerMono)
                .expectNext(dancer)
                .expectComplete()
                .verify();
    }
}
