package hello.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Odd {
    private String torneio;
    private Partida partida;
    private String bookmaker;
    private String market;
    private double odd;
    private String name;
}
