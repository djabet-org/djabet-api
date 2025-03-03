package hello;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hello.data.DashboardDTO;
import hello.domain.BettingService;
import hello.domain.PartidaArbs;
import hello.domain.PartidaEVs;
import hello.domain.PartidaOdds;
import hello.dto.Partida;
import hello.dto.ValueBet;
import hello.model.EVFilter;
import hello.service.DoubleService;
import hello.service.ValueBetService;

@RestController
@RequestMapping("/api/sports")
public class SportingBetController {

    @Autowired
    private BettingService _bettingService;

    private Logger _log = Logger.getLogger(getClass().getName());

    @CrossOrigin
    @GetMapping(path = "/valuebet", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getValueBets(@RequestParam("bankroll") double bankroll,
            @RequestParam("minEV") Optional<Double> minEV, @RequestParam("maxEV") Optional<Double> maxEV,
            @RequestParam("minOdd") Optional<Double> minOdd, @RequestParam("maxOdd") Optional<Double> maxOdd,
            @RequestParam("live") Optional<Boolean> live,
            @RequestParam("prematch") Optional<Boolean> prematch,
            @RequestParam("markets") Optional<String> markets) {
        try {
            _log.info("New request!");
            EVFilter evFilter = EVFilter.builder()
                    .minEv(minEV.orElse(0.0))
                    .maxEv(maxEV.orElse(Double.MAX_VALUE))
                    .maxOdd(maxOdd.orElse(Double.MAX_VALUE))
                    .minOdd(minOdd.orElse(0.0))
                    .markets(markets.orElse(""))
                    .live(live.orElse(null))
                    .prematch(prematch.orElse(null))
                    .build();
            List<PartidaOdds> odds = _bettingService.getOdds(evFilter);
            List<PartidaEVs> evs = _bettingService.calculateEVs(odds, evFilter);
            String evsAsJson = new ObjectMapper().writeValueAsString(evs);
            return ResponseEntity.ok().body(evsAsJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @CrossOrigin
    @GetMapping(path = "/arbs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getArbs(@RequestParam("bankroll") double bankroll,
            @RequestParam("minArb") Optional<Double> minArb, @RequestParam("maxArb") Optional<Double> maxArb,
            @RequestParam("minOdd") Optional<Double> minOdd, @RequestParam("maxOdd") Optional<Double> maxOdd,
            @RequestParam("live") Optional<Boolean> live,
            @RequestParam("prematch") Optional<Boolean> prematch,
            @RequestParam("sports") Optional<String> sports,
            @RequestParam("notSports") Optional<String> notSports,
            @RequestParam("markets") Optional<String> markets) {
        try {
            _log.info("New request - Arbs!");
            EVFilter evFilter = EVFilter.builder()
                    .minArb(minArb.orElse(0.0))
                    .maxArb(maxArb.orElse(Double.MAX_VALUE))
                    .maxOdd(maxOdd.orElse(Double.MAX_VALUE))
                    .minOdd(minOdd.orElse(0.0))
                    .markets(markets.orElse("h2h"))
                    .sports(sports.orElse(""))
                    .live(live.orElse(null))
                    .prematch(prematch.orElse(null))
                    .notSports(notSports.orElse(""))
                    .build();


            List<PartidaOdds> odds = _bettingService.getOdds(evFilter);

            List<PartidaArbs> arbs = _bettingService.getArbs(odds, evFilter);

            String arbsJson = new ObjectMapper().writeValueAsString(arbs);

            return ResponseEntity.ok().body(arbsJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}