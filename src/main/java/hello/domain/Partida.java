package hello.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Partida {
    private String id;
    private String sportKey; 
    private String name;
    private String homeTeam;
    private String awayTeam;
    private String horario;
    private String torneio;
}
