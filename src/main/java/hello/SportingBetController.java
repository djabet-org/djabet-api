package hello;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hello.data.DashboardDTO;
import hello.dto.Partida;
import hello.dto.ValueBet;
import hello.service.DoubleService;
import hello.service.ValueBetService;

@RestController
public class SportingBetController {

    @Autowired
    private ValueBetService _valueBetService;

    private Logger _log = Logger.getLogger(getClass().getName());

    @CrossOrigin
    @GetMapping(path = "/api/sports/valuebet", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getValueBets(@RequestParam("bankroll") double bankroll) {
        try {
            Map<Partida, List<ValueBet>> evs = _valueBetService.getValueBets(bankroll);
            String evsAsJson = new ObjectMapper().writeValueAsString(evs);
            return ResponseEntity.ok().body(evsAsJson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}