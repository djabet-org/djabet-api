package hello.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Odd {
    private String bookmaker;
    private String market;
    private String link;
    private double odd;
    private Outcome outcome;

    public String getOutcome() {
        return outcome.getOutcome();
    }
}
