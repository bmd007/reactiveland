package reactiveland.season2.episode2;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertTrue;


class TestingInternet {

    record Advice(Slip slip){
       public boolean isLongEnough(){
            return slip.advice.length() > 3;
        }
    }
    record Slip(String id, String advice){}

    @Test
    void internetCanGiveAdviceLongerThan3Words(){
        //given
        var adviserHost = "https://api.adviceslip.com";
        var getAdvicePath = "/advice";
        WebTestClient testClient = WebTestClient
                .bindToServer()
                .baseUrl(adviserHost)
                .build();
        //when
        //todo

        //then
        //hint: the response content type is text. not json
        // todo
    }

}
