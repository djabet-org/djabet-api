package hello.repository;

import java.util.List;

import hello.dto.Odd;
import hello.dto.Partida;

public interface BetRepository {

    List<Odd> getOddsFor(Partida partida);

}
