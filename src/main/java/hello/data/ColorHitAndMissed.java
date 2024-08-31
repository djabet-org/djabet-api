package hello.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColorHitAndMissed {
    private int hit;
    private int missed;
    private String color;

    public void hitting() {
        hit += 1;
    }


    public void missing() {
        missed += 1;
    }

    public int getTotal() {
        return hit + missed;
    }


}
