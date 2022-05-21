package reactiveland.season1.episode4;

import org.springframework.stereotype.Service;

@Service
public class Notifier {

    private final HungryGuy hungryGuy;

    public Notifier(HungryGuy hungryGuy) {
        this.hungryGuy = hungryGuy;
    }

    void notifyHungry(String foodName) {
        hungryGuy.getFood(foodName);
    }
}
