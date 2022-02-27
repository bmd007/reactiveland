package reactiveland.season1.episode1;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JustABox {

    record Stone(){String getColor(){return "Red";}}

    @Test
    void putARedStoneInABoxAndPrintTheColor(){
        //given a stone
            Stone redStone = new Stone();
        //when creating a Mono with a red stone in it
             Mono<Stone> stoneBox = Mono.just(redStone);
             String color = stoneBox.block().getColor();
        //then print it's color
            System.out.println(color);
    }

    @Test
    void putARedStoneInABoxAndAssertTheColor(){
        //given a stone
        Stone redStone = new Stone();
        //when creating a Mono with a red stone in it
        Mono<Stone> stoneBox = Mono.just(redStone);
        //then assert that it's color is Red
        stoneBox.subscribe(stone -> {
            String stoneColor = stone.getColor();
            assertEquals("Red", stoneColor);
        });
    }

    @Test
    void createAMonoOfRedStoneAndVerifyIt(){
        //given a stone
        Stone redStone = new Stone();
        //when creating a Mono with a red stone in it
        Mono<Stone> stoneBox = Mono.just(redStone);
        //then assert steps and content of the mono
        StepVerifier.create(stoneBox)
                .assertNext(next -> next.getColor().equals("Red"))
                .expectComplete()
                .verify();
    }
}
