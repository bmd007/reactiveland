package reactiveland.season2.episode3;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@ActiveProfiles("test")
@ExtendWith({SpringExtension.class})
@SpringBootTest
@AutoConfigureWebTestClient
class BecomingBrowserHistory {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void internetCanGiveAdviceLongerThan3Words() {
        //given
        var getAdvicePath = "/api/advice";
        //when
        webTestClient
                .get()
                .uri(getAdvicePath)
                .exchange()
                //then
                .expectStatus()
                .isOk()
                .expectHeader().valueEquals("Content-Type", APPLICATION_JSON_VALUE)
                .expectBody(Advice.class)
                .value(advice -> assertTrue(advice.isLongEnough()));
    }

    @Test
    void internetCanCountEvenNumbers(){
        //given
        var getAdvicePath = "/api/numbers/count/evens";
        var numbers = Flux.fromIterable(IntStream.rangeClosed(0, 10).boxed().toList());
        //when
        webTestClient
                .post()
                .uri(getAdvicePath)
                .body(numbers, Integer.class)
                .exchange()
                //then
                .expectStatus()
                .isOk()
                .expectHeader().valueEquals("Content-Type", APPLICATION_JSON_VALUE)
                .expectBody(Long.class)
                .value(numberOfEvens -> assertEquals(6, numberOfEvens));
    }

   @Test
    void internetCanEchoEvenNumbers(){
        //given

    }

}
