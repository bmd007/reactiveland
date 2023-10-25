package reactiveland.show.season1.episode5;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


class MoreThanOneFish {

    class Fish {
        String name;
        int size;
        LocalDateTime catchTime;
        private boolean hasHead;

        public Fish(String name, int size, LocalDateTime catchTime) {
            this.name = name;
            this.size = size;
            this.catchTime = catchTime;
            this.hasHead = true;
        }

        public Fish() {
            name = "fish_" + System.currentTimeMillis();
            size = 3;
            catchTime = LocalDateTime.now().minusHours(4);
            hasHead = true;
        }

        public Fish cutHead() {
            size = size - 1;
            hasHead = false;
            return this;
        }

        public boolean hasHead() {
            return hasHead;
        }
    }

    boolean isNotTooBig(Fish fish) {
        return fish.size < 5;
    }

    boolean isFreshEnough(Fish fish) {
        return fish.catchTime.isAfter(LocalDateTime.now().minusDays(1L));
    }

    @Test
    void assertTheFishesInTheRiver() {
        //given
        var fish1 = new Fish();
        var fish2 = new Fish();
        var fish3 = new Fish();
        //when
        Flux<Fish> river = Flux.just(fish1, fish2, fish3);
        // then
        StepVerifier.create(river)
                .expectNext(fish1, fish2, fish3)
                .expectComplete()
                .verify();
    }

    @Test
    void cutTheHeadOfFishes() {
        //given
        var fishBatch1 = List.of(new Fish(), new Fish());
        var fishBatch2 = Set.of(new Fish(), new Fish());
        //when
        Flux<Fish> fishCleaningMachine = Flux.fromIterable(fishBatch1)
                .concatWith(Flux.fromIterable(fishBatch2))
                .map(Fish::cutHead);
        //then
        StepVerifier.create(fishCleaningMachine)
                .expectNextMatches(fish -> !fish.hasHead())
                .expectNextMatches(fish -> !fish.hasHead())
                .expectNextMatches(fish -> !fish.hasHead())
                .expectNextMatches(fish -> !fish.hasHead())
                .expectComplete()
                .verify();
    }

    @Test
    void onlyUseFreshAndSmallFishesForSoup() {
        //given
        var frozenFish = new Fish("frozen fish", 10, LocalDateTime.now().minusMonths(1L));
        var freshFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(2L));
        var bigNotFreshFish = new Fish("caught way back", 6, LocalDateTime.now().minusYears(2L));
        var freshEnoughFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(22L));
        //when
        Flux<Fish> soupFishes = Flux.just(frozenFish, freshFish, bigNotFreshFish, freshEnoughFish)
                .filter(this::isFreshEnough)
                .filter(this::isNotTooBig);
        //then
        StepVerifier.create(soupFishes)
                .expectNext(freshFish)
                .expectNext(freshEnoughFish)
                .expectComplete()
                .verify();
    }

    record FishBatch(Set<Fish> fishSet) {
    }

    @Test
    void only2FreshFishesInTheBatch() {
        //given
        var frozenFish = new Fish("frozen fish", 10, LocalDateTime.now().minusMonths(1L));
        var freshFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(2L));
        var bigNotFreshFish = new Fish("caught way back", 6, LocalDateTime.now().minusYears(2L));
        var freshEnoughFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(22L));
        var fishBatch = new FishBatch(Set.of(bigNotFreshFish, freshEnoughFish, frozenFish, freshFish));
        //when
        var freshFishBatch = Mono.just(fishBatch)
                .flatMapIterable(FishBatch::fishSet)
                .filter(this::isFreshEnough);
        //then
        StepVerifier.create(freshFishBatch)
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }
}
