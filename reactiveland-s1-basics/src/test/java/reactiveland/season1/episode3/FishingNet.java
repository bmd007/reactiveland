package reactiveland.season1.episode3;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishingNet {

    @Test
    void noEvenNumberInTheMono(){
        //given
        int number = 44;
        //when
        Mono<Integer> oddNumberOnlyMono = Mono.just(number)
                .filter(n -> n % 2 != 0);

        //then
        StepVerifier.create(oddNumberOnlyMono)
                .expectNextCount(0)
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
        return Mono.just(new Fish("fresh fish", 8, LocalDateTime.now().minusMinutes(30L).truncatedTo(ChronoUnit.MINUTES)));
    }

    @Test
    void useFrozenFishIfNoBigEnoughFreshFish(){
        //given
        var frozenFish = new Fish("frozen fish", 10, LocalDateTime.now().minusMonths(1L));
        var freshFish = new Fish("caught today", 4, LocalDateTime.now().minusHours(2L));
        var bigNotFreshFish = new Fish("caught way back", 6, LocalDateTime.now().minusYears(2L));

        //when
        Mono<Fish> ourFish = Mono.just(bigNotFreshFish)
                .filter(this::isFishBigEnough)
                .filter(this::isFishFreshEnough)
                .defaultIfEmpty(frozenFish);
        //then
        StepVerifier.create(ourFish)
                .expectNext(frozenFish)
                .expectComplete()
                .verify();
    }

    private class FishNotFoundException extends Exception {
        public FishNotFoundException(String message) {
            super(message);
        }
    }

    @Test
    void notFoundExceptionWhenNoBigEnoughFreshFish(){
        //given
        var fish = new Fish("bought today", 8, LocalDateTime.now().minusDays(5L));
        //when
        Mono<Fish> bigEnoughFreshFish = Mono.just(fish)
                    .filter(this::isFishBigEnough)
                    .filter(this::isFishFreshEnough)
                    .switchIfEmpty(Mono.error(new FishNotFoundException("no big enough fresh fish")));
        //then
        StepVerifier.create(bigEnoughFreshFish)
                .expectErrorMatches(error -> error instanceof FishNotFoundException)
                .verify();
    }

    @Test
    void catchFishIfNoBigEnoughFreshFish(){
        //given
        var fish = new Fish("bought today", 8, LocalDateTime.now().minusDays(5L));
        var expectedFish = new Fish("fresh fish", 8, LocalDateTime.now().minusMinutes(30L).truncatedTo(ChronoUnit.MINUTES));
        //when
            Mono<Fish> bigEnoughFreshFish = Mono.just(fish)
                    .filter(this::isFishBigEnough)
                    .filter(this::isFishFreshEnough)
                    .switchIfEmpty(catchABigFish());
        //then
        //todo
        StepVerifier.create(bigEnoughFreshFish)
                .expectNext(expectedFish)
                .expectComplete()
                .verify();
    }

    @Test
    void nullIsNotEmptyButIsError(){
        //when
        Mono mono = Mono.<Integer>just(2)
                .map(a -> null)
                .onErrorMap(e -> new IllegalArgumentException())
                .defaultIfEmpty(3);
        //then
        StepVerifier.create(mono)
                .expectErrorMatches(error -> error instanceof IllegalArgumentException)
                .verify();
    }

}
