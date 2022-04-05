package reactiveland.season1.episode4;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WaterAndElectricity {

    record Fish(String name, int size, LocalDateTime catchTime){}
    record Potato(){}
    record Food(String name, Fish fish, Potato potato){}

    @Mock
    HungryGuy hungryGuy;

    @InjectMocks
    Notifier notifier;

    Mono<Fish> catchAFish(){
        return Mono.just(new Fish("fresh fish", 8, LocalDateTime.now().minusMinutes(30L)));
    }

    Mono<Food> cookADish(Fish fish){
        return Mono
                .delay(Duration.ofSeconds(5L))
                .map(ignore -> new Food("seafood", fish, new Potato()));
    }

    @Test
    void prepareADishAndNotifyTheHungryGuy(){
        //when
        Mono<Food> dishMono = catchAFish()
                .flatMap(this::cookADish)
                .doOnNext(food -> notifier.notifyHungry(food.name()));
        //then
        StepVerifier.create(dishMono)
                .assertNext(dish -> assertEquals(8, dish.fish.size))
                .expectComplete()
                .verify();
        verify(hungryGuy, timeout(3)).getFood("seafood");
    }

}
