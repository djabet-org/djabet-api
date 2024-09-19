package hello.dto;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SureBet {

    private String torneio;
    private String partidaId;
    private String partida;
    private String awayBookmaker;
    private String homeBookmaker;
    private String drawBookmaker;
    private String homeTeam;
    private String awayTeam;
    private double arbs;
    private double homeOdd;
    private double awayOdd;
    private double drawOdd;
    private String horario;
    private String market;
    private double betAmmount1;
    private double betAmmount2;

    public boolean isLive() {
        Date date = Date.from(Instant.parse(horario));
        return !date.after(Date.from(Instant.now()));
    }


    @Override
    public String toString() {
            return String.format(" +Arb: %.2f%% | $ Bet: R$%.2f @ %.2f (%s/%s), R$%.2f @ %.2f (%s/%s) | %s | %s | (%s) | %s ",
                            arbs, betAmmount1, homeOdd, homeTeam, homeBookmaker, betAmmount2, awayOdd, awayTeam, 
                            awayBookmaker, torneio, partida, market, horario
                                );
    }
}
