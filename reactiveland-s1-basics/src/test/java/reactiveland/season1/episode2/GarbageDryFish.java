package reactiveland.season1.episode2;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GarbageDryFish {

    private final static Logger LOGGER = LoggerFactory.getLogger(GarbageDryFish.class);

    record Car(String name, String maxSpeedInKMH){}

    @Test
    void assertMaxSpeedInMeterPerSecond(){
        //given
        var bugattiChiron = new Car("Bugatti Chiron", "381");
        //when
//        Mono<Double> actualMaxSpeedMono = ;//todo
//        //then
//        StepVerifier.create(actualMaxSpeedMono)
//                .assertNext(maxSpeedInMPS -> maxSpeedInMPS.equals(105.833))
//                .expectComplete()
//                .verify();
    }

    @Test
    void illegalAccessExceptionWhenInvalidMaxSpeed(){
        //given
        var bugattiChiron = new Car("Bugatti Chiron", "380 and something");
        //when turning speed from KPH to MPS
        Mono<Double> maxSpeedMono = Mono.just(bugattiChiron)
                .map(Car::maxSpeedInKMH)
                .map(Integer::valueOf)
                .map(speed -> speed / 3.6)
                .doOnError(error -> LOGGER.error("error before transformation", error))
                //todo
                ;
        //then
        StepVerifier.create(maxSpeedMono)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
