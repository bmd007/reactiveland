package reactiveland.season2.episode3;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api")
public class DummyResource {

    @GetMapping(path = "/advice")
    public Mono<Advice> advice() {
        return Mono.just(new Advice("go outside"));
    }

    @PostMapping(path = "/numbers/count/evens")
    public Mono<Long> countEvenNumbers(@RequestBody Flux<Integer> numbers){
        return numbers.filter(i -> i % 2 == 0).count();
    }

    @PostMapping(path = "/numbers/filter/evens")
    public Flux<Integer> filterEvenNumbers(@RequestBody Flux<Integer> numbers) {
        return numbers.filter(i -> i % 2 == 0);
    }

}
