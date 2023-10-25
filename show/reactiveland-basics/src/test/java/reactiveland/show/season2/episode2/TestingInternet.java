package reactiveland.show.season2.episode2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;


class TestingInternet {

    record Advice(Slip slip) {
        record Slip(String id, String advice) {
        }

        public boolean isLongEnough() {
            return slip.advice.length() > 3;
        }
    }

    @Test
    void internetCanGiveAdviceLongerThan3Words() {
        //given
        var objectMapper = new ObjectMapper();
        var adviserHost = "https://api.adviceslip.com";
        var getAdvicePath = "/advice";
        WebTestClient testClient = WebTestClient
                .bindToServer()
                .baseUrl(adviserHost)
                .build();
        //when
        testClient
                .get()
                .uri(getAdvicePath)
                .exchange()
                //then
                .expectStatus()
                .isOk()
                .expectHeader().valueEquals("Content-Type", "text/html; charset=UTF-8")
                .expectBody(String.class)
                .value(advicePlainText -> {
                    try {
                        var advice = objectMapper.readValue(advicePlainText, Advice.class);
                        assertTrue(advice.isLongEnough());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
