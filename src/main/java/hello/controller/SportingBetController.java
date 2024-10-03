// package hello.controller;

// import java.util.List;
// import java.util.Optional;
// import java.util.logging.Logger;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.fasterxml.jackson.databind.ObjectMapper;

// import hello.dto.ValueBet;
// import hello.model.EVFilter;
// import hello.service.ValueBetService;

// // @RestController
// public class SportingBetController {

//     @Autowired
//     private ValueBetService _valueBetService;

//     private Logger _log = Logger.getLogger(getClass().getName());

//     @CrossOrigin
//     @GetMapping(path = "/api/sports/valuebet2", produces = MediaType.APPLICATION_JSON_VALUE)
//     public ResponseEntity<String> getValueBets2()
//             {
//                 return ResponseEntity.ok().build();

//             }

//     @CrossOrigin
//     @GetMapping(path = "/api/sports/valuebet", produces = MediaType.APPLICATION_JSON_VALUE)
//     public ResponseEntity<String> getValueBets(@RequestParam("bankroll") double bankroll,
//             @RequestParam("minEv") Optional<Double> minEv, @RequestParam("maxEv") Optional<Double> maxEv) {
//         try {
//             _log.info("creuuuuu");
//             System.out.println("heyyy");
//             EVFilter evFilter = EVFilter.builder()
//                     .minEv(minEv.orElse(1.0))
//                     .maxEv(maxEv.orElse(Double.MAX_VALUE))
//                     .build();

//             List<ValueBet> evs = _valueBetService.getValueBets(bankroll);
//             String evsAsJson = new ObjectMapper().writeValueAsString(evs);
//             return ResponseEntity.ok().body(evsAsJson);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().build();
//         }
//     }
// }