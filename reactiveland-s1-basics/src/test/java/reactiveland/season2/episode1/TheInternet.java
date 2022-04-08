package reactiveland.season2.episode1;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TheInternet {

    @Test
    void getAnAdviceFromInternet(){
        //given
        var adviserHost = "https://api.adviceslip.com";
        var getAdvicePath = "/advice";
        //when
        //todo
        Mono<String> advice = null;
        //then
        // todo
    }

    @Test
    void countCompletedTodos(){
        //given
        var todosHost = "https://gorest.co.in/public/v2";
        var todosPath = "/todos";
        //when todo
        //then todo
    }

}
