package reactiveland.season1.episode4;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class HungryGuy {

    public void getFood(String foodName) {
        System.out.println(" eating "+ foodName + "  " + LocalTime.now());
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(foodName + " was tasty " + "  " + LocalTime.now());
    }
}
