package reactiveland.season1.episode1;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JustABox {

    record Stone() {
        String getColor() {
            return "Red";
        }
    }

    @Test
    void putARedStoneInABoxAndPrintTheColor() {
        //given a stone
        Stone redStone = new Stone();
        //when creating a Mono with a red stone in it
        Mono<Stone> stoneBox = Mono.just(redStone);
        String color = stoneBox.block().getColor();
        //then print it's color
        System.out.println(color);
    }

    @Test
    void putARedStoneInABoxAndAssertTheColor() throws InterruptedException {
        //when creating a Mono with a red stone in it
        Mono<Stone> stoneBox = putARedStoneInABoxAfter5Seconds();
        //then assert that it's color is Red with no blocking behaviour
        stoneBox.subscribe(stone -> assertEquals("Blue", stone.getColor()));
        Thread.sleep(6000);
    }

    @Test
    void createAMonoOfRedStoneAndVerifyIt() {
        //given a stone
        Stone redStone = new Stone();
        //when creating a Mono with a red stone in it
        Mono<Stone> stoneBox = Mono.just(redStone);
        //then assert steps and content of the mono
        StepVerifier.create(stoneBox)
                .assertNext(stone -> assertEquals("Red", stone.getColor()))
                .expectComplete()
                .verify();
    }

    @Test
    void createAnEmptyBoxAndVerifyIt() {
        Mono<Object> emptyBox = Mono.empty();
        StepVerifier.create(emptyBox)
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    Mono<Stone> putARedStoneInABoxAfter5Seconds() {
        return Mono.delay(Duration.ofSeconds(5)).map(ignore -> new Stone());
    }
}
