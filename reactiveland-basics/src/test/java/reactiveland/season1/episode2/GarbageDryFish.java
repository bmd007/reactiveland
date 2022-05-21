package reactiveland.season1.episode2;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GarbageDryFish {

    private final static Logger LOGGER = LoggerFactory.getLogger(GarbageDryFish.class);

    record Car(String name, String maxSpeedInKMH) {
    }

    @Test
    void assertMaxSpeedInMeterPerSecond() {
        //given
        var bugattiChiron = new Car("Bugatti Chiron", "381");
        //when
        Mono<Long> actualMaxSpeedMono = Mono.just(bugattiChiron)
                .map(Car::maxSpeedInKMH)
                .map(Double::valueOf)
                .map(KMHSpeed -> KMHSpeed / 3.6)
                .map(Math::round);
        //then
        StepVerifier.create(actualMaxSpeedMono)
                .assertNext(maxSpeedInMPS -> assertEquals(106, maxSpeedInMPS))
                .expectComplete()
                .verify();
    }

    private class CarException extends Exception {
        public CarException(String message) {
            super(message);
        }
    }

    @Test
    void carExceptionWhenInvalidMaxSpeed() {
        //given
        var bugattiChiron = new Car("Bugatti Chiron", "380 and something");
        //when turning speed from KPH to MPS
        Mono<Double> maxSpeedMono = Mono.just(bugattiChiron)
                .map(Car::maxSpeedInKMH)
                .map(Integer::valueOf)
                .map(speed -> speed / 3.6)
                .doOnError(error -> LOGGER.error("error", error))
                .onErrorMap(error -> new CarException("wrong car speed"));
        //then
        StepVerifier.create(maxSpeedMono)
                .expectErrorMatches(throwable -> throwable.getMessage().equals("wrong car speed") &&
                        throwable.getClass().equals(CarException.class))
                .verify();
    }
}
