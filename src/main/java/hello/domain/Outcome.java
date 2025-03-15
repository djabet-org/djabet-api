package hello.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Outcome {
    private double odd;
    private double totalsPoint;
    private String name; 

    public String getOutcome() {
        // if (totalsPoint > 0) {
        //     return String.format("%s %.2f", name, totalsPoint);
        // }

        return name; 

    }
}
