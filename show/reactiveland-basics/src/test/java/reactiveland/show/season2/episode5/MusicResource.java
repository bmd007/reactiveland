package reactiveland.show.season2.episode5;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactiveland.season2.episode3.Advice;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/music")
public class MusicResource {

    @GetMapping
    Flux<Music> musics() {
        return Flux.just(new Music("ghadder", "dahloo", LocalDateTime.now().minusYears(2)));
    }

}
