package reactiveland.season2.episode1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TheInternet {

    private record Advice(Integer id, String advice){}
    private record Body(Advice slip){}

    @Test
    void getAnAdviceFromInternet(){
        //given
        var adviserHost = "https://api.adviceslip.com";
        var getAdvicePath = "/advice/81";
        ObjectMapper objectMapper = new ObjectMapper();
        //when
        Mono<String> advice = WebClient.builder()
                .baseUrl(adviserHost)
                .build()
                .get()
                .uri(getAdvicePath)
                .accept(MediaType.TEXT_HTML)
                .retrieve()
                .bodyToMono(String.class)
                .map(stringBody -> {
                    try {
                        return objectMapper.readValue(stringBody, Body.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(body -> body.slip.advice)
                .doOnError(e -> System.out.println("Error!!" + e));
        //then
        StepVerifier.create(advice)
                .expectNext("Age is of no importance, unless you are a cheese.")
                .expectComplete()
                .verify();
    }

    private record Todo(int id, int user_id, String title, String date, String status){}

    @Test
    void countCompletedTodos(){
        //given
        var todosHost = "https://gorest.co.in/public/v2";
        var todosPath = "/todos";
        ObjectMapper objectMapper = new ObjectMapper();
        //when
        Mono<Long> numberOfTodos = WebClient.builder()
                .baseUrl(todosHost)
                .build()
                .get()
                .uri(todosPath)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Todo.class)
                .map(Todo::status)
                .count()
                .doOnError(e -> System.out.println("Error!!" + e));
        //then
        StepVerifier.create(numberOfTodos)
                .expectNext(20L)
                .expectComplete()
                .verify();
    }

    @Test
    void randomSentence(){
        var randomSentenceMono = WebClient.builder()
                .baseUrl("https://random-num-x5ht4amjia-uc.a.run.app")
                .build()
                .get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .map(Integer::valueOf)
                .flatMapMany(randomNumber -> Flux.range(1, randomNumber))
                .flatMap(ignore -> WebClient.builder()
                        .baseUrl("https://random-word-x5ht4amjia-uc.a.run.app")
                        .build()
                        .get()
                        .uri("/")
                        .retrieve()
                        .bodyToMono(String.class)
                )
                .reduce((s, s2) -> s.concat(" ").concat(s2))
                .log();

        StepVerifier.create(randomSentenceMono)
                .expectNext(" wrong ")
                .expectComplete()
                .verify();
    }

}
