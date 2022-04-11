package reactiveland.season1.episode5;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


class MoreThanOneFish {

    class Fish{
        String name;
        int size;
        LocalDateTime catchTime;

        public Fish(String name, int size, LocalDateTime catchTime) {
            this.name = name;
            this.size = size;
            this.catchTime = catchTime;
        }

        public Fish(){
            name  = "fish_"+ System.currentTimeMillis();
            size = 3; 
            catchTime = LocalDateTime.now().minusHours(4);
        }
        
        public Fish cutHead(){
            size = size - 1;
            return this;
        }
    }

    boolean isNotTooBig(Fish fish){
        return fish.size < 5;
    }

    boolean isFreshEnough(Fish fish){
        return fish.catchTime.isAfter(LocalDateTime.now().minusDays(1L));
    }

    @Test
    void assertTheFishesInTheRiver(){
        //given
        var fish1 = new Fish();
        var fish2 = new Fish();
        var fish3 = new Fish();
        //when
        Flux<Fish> river = null; //todo
        // then
        //todo
    }

    @Test
    void cutTheHeadOfFishes(){
        //given
        var fishBatch1 = List.of(new Fish(), new Fish());
        var fishBatch2 = Set.of(new Fish(), new Fish());
        //when
        Flux<Fish> fishCleaningMachine = null;//todo
        //then
        //todo
    }

    @Test
    void onlyUseFreshAndSmallFishesForSoup(){
        //given
        var frozenFish = new Fish("frozen fish", 10, LocalDateTime.now().minusMonths(1L));
        var freshFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(2L));
        var bigNotFreshFish = new Fish("caught way back", 6, LocalDateTime.now().minusYears(2L));
        var freshEnoughFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(22L));
        //when
        Mono<Fish> soupFishes = null;
        //then
        StepVerifier.create(soupFishes)
                .expectNext(freshFish)
                .expectNext(freshEnoughFish)
                .expectComplete()
                .verify();
    }

    record FishBatch(Set<Fish> fishSet){}

    @Test
    void only2FreshFishes(){
        //given
        var frozenFish = new Fish("frozen fish", 10, LocalDateTime.now().minusMonths(1L));
        var freshFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(2L));
        var bigNotFreshFish = new Fish("caught way back", 6, LocalDateTime.now().minusYears(2L));
        var freshEnoughFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(22L));
        var fishBatch = new FishBatch(Set.of(bigNotFreshFish, freshEnoughFish, frozenFish, freshFish));
        var fishBatchMono = Mono.just(fishBatch);
        //when
        //todo
        //then
        //todo
    }
}
