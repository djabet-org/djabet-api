package hello.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValueBet {
    private double ev;
    private String evPercentage;
    private String market;
    private String bookmaker;
    private double odd;
    private String outcome;
    private double sharpOdd;
    private double betAmmount;

}
