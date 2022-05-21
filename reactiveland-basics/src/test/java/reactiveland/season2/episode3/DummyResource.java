package reactiveland.season2.episode3;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api")
public class DummyResource {

    @GetMapping(path = "/")
    public Mono<Advice> advice() {
        return Mono.empty();
    }

    @PostMapping(path = "/numbers")
    public Mono<Long> countEvenNumbers(@RequestBody Integer numbers){
        return Mono.empty();
    }

}
