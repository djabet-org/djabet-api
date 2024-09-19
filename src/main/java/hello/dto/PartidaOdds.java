package hello.dto;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartidaOdds {
    private Partida partida;
    private List<Bookmaker> bookmakers;
}