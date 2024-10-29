package hello.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArbBet {
    private double arb;
    private String arbPercentage;
    private String market;
    private String bookmakerA;
    private String bookmakerB;
    private double oddA;
    private double oddB;
    private String outcomeA;
    private String outcomeB;
    private double betAmmount;

}
