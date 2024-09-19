package hello.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValueBet {
    private double ev;
    private String market;
    private Partida partida;
    private String torneio;
    private String bookmaker;
    private double odd;
    private String forWhat;
    private double sharpOdd;
    private double betAmmount;

}
