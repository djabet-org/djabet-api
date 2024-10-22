package hello.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    private long horarionUnix;

    public boolean isLive() {
        return Instant.ofEpochSecond(horarionUnix).isBefore(Instant.now());
    }
}
