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
    @RequestParam("minEV") Optional<Double> minEV, @RequestParam("maxEV") Optional<Double> maxEV) {
        try {
            _log.info("New request!");
            List<PartidaOdds> odds = _bettingService.getOdds();
            EVFilter evFilter = EVFilter.builder().minEv(minEV.orElse(1.0)).maxEv(maxEV.orElse(Double.MAX_VALUE)).build();
            List<PartidaEVs> evs = _bettingService.calculateEVs(odds);
            String evsAsJson = new ObjectMapper().writeValueAsString(evs);
            // _log.info(evsAsJson);
            return ResponseEntity.ok().body(evsAsJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}