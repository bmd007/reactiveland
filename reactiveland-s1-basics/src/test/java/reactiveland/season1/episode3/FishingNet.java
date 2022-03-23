package reactiveland.season1.episode3;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishingNet {

    @Test
    void noEvenNumberInTheMono(){
        //given
        int number = 44;
        //when
        Mono<Integer> oddNumberOnlyMono = Mono.just(number)//todo

        //then
        //todo
        StepVerifier.create(oddNumberOnlyMono)
                .expectNext(2)
                .expectComplete()
                .verify();
    }

    record Fish(String name, int size, LocalDateTime catchTime){}

    boolean isFishBigEnough(Fish fish){
        return fish.size >= 5;
    }

    boolean isFishFreshEnough(Fish fish){
        return fish.catchTime.isAfter(LocalDateTime.now().minusDays(1L));
    }

    Mono<Fish> catchABigFish(){
        return Mono.just(new Fish("fresh fish", 8, LocalDateTime.now().minusMinutes(30L)));
    }

    @Test
    void useFrozenFishIfNoBigEnoughFreshFish(){
        //given
        var frozenFish = new Fish("frozen fish", 10, LocalDateTime.now().minusMonths(1L));
        var freshFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(2L));
        //when
            Mono.just(freshFish)//todo
        //then
        // todo

    }

    @Test
    void catchFishIfNoBigEnoughFreshFish(){
        //given
        var fish = new Fish("bought today", 8, LocalDateTime.now().minusDays(5L));
        //when
            Mono.just(fish)//todo
        //then
        //todo


    }


}
