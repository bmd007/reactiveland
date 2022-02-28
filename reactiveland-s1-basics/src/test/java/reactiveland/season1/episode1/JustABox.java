package reactiveland.season1.episode1;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JustABox {

    record Stone(){
        String getColor(){
            return "Red";
        }
    }

    @Test
    void putARedStoneInABoxAndPrintTheColor(){
        //given a stone
            Stone redStone = new Stone();
        //when creating a Mono with a red stone in it
             Mono<Stone> stoneBox = //todo
             String color = stoneBox.//todo
        //then print it's color
            System.out.println(color);
    }

    @Test
    void putARedStoneInABoxAndAssertTheColor(){
        //when creating a Mono with a red stone in it
        Mono<Stone> stoneBox = putARedStoneInABoxAfter5Seconds();
        //then assert that it's color is Red with no blocking behaviour
         assertEquals("Red", stoneColor);//todo
    }

    @Test
    void createAMonoOfRedStoneAndVerifyIt(){
        //given a stone
        Stone redStone = new Stone();
        //when creating a Mono with a red stone in it
        Mono<Stone> stoneBox = //todo;
        //then assert steps and content of the mono
        StepVerifier//todo
                .verify();
    }

    @Test
    void createAnEmptyBoxAndVerifyIt(){

    }


    Mono<Stone> putARedStoneInABoxAfter5Seconds(){
        return Mono.delay(Duration.ofSeconds(5)).map(ignore -> new Stone());
    }
}
