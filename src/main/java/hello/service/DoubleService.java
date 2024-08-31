package hello.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import hello.ExcelHelper;
import hello.Roll;
import hello.data.CoresPercentualDTO;
import hello.repository.DoubleRepository;
import lombok.Getter;
import lombok.Setter;

@Service
public class DoubleService {

    private Logger _log = Logger.getLogger(getClass().getName());


    @Autowired
    private DoubleRepository repository;

    @Autowired
    private ExcelHelper excelHelper;

    public void save(Roll roll) {
        repository.save(roll);
    }

    public List<Roll> fetch(int qtd, String sort, String platform) {
        // return repository.findByPlatform(platform);
        Sort.Direction sortDirection = "asc".equals(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<Roll> page = repository.findByPlatform(platform,
                PageRequest.of(0, qtd, Sort.by(sortDirection, "created")));

        return page.get().collect(Collectors.toList());
    }

    public List<Roll> upload(MultipartFile file) {
        try {
            return excelHelper.excelToRolls(file.getInputStream());
            // rolls.iterator().forEachRemaining(r -> _log.info(r.toString()));
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public CoresPercentualDTO calculateCoresPercentual(List<Roll> rolls) {
        Map<String, Long> r = rolls
                .stream()
                .collect(Collectors.groupingBy(Roll::getColor, Collectors.counting()));

                long total = r.get("black") + r.get("red") + r.get("white");

        return CoresPercentualDTO.builder()
                .black(r.get("black"))
                .white(r.get("white"))
                .red(r.get("red"))
                .percentageBlack(_toPercentage(r.get("black"), total))
                .percentageRed(_toPercentage(r.get("red"), total))
                .percentageWhite(_toPercentage(r.get("white"), total))
                .build();
    }

    private String _toPercentage(long qtdColor, long total) {
        return String.format("%.2f%%", (float) qtdColor/total*100);
    }

//     public NumerosCoresProbabilidadesDTO calcularNumerosCoresProbabilidades(List<Roll> rolls) {
//         Map<Integer, NumerosCoresProbabilidadesDTO> probabilidades = new HashMap<>();

//         int galho = 1;

//         for (int i=0; i< rolls.size(); i++) {
//             boolean foundBlack = rolls.subList(i, i+galho).stream().anyMatch( r -> r.getColor().equals("black"));
//             boolean foundRed = rolls.subList(i, i+galho).stream().anyMatch( r -> r.getColor().equals("red"));
//             boolean foundWhite = rolls.subList(i, i+galho).stream().anyMatch( r -> r.getColor().equals("white"));

//             Roll roll = rolls.get(i);

//             ColorHitAndMissed black = probabilidades.containsKey(roll.getRoll()) ? probabilidades.get(roll.getRoll()).getBlackHitAndMissed() : ColorHitAndMissed.builder().color("black").build();
//             ColorHitAndMissed red = probabilidades.containsKey(roll.getRoll()) ? probabilidades.get(roll.getRoll()).getRedHitAndMissed() : ColorHitAndMissed.builder().color("red").build();
//             ColorHitAndMissed white = probabilidades.containsKey(roll.getRoll()) ? probabilidades.get(roll.getRoll()).getWhiteHitAndMissed() : ColorHitAndMissed.builder().color("white").build();

//              if (foundBlack) {
//                 black.hitting();
//              } else {
//                 black.missing();
//              }

//              if (foundRed) {
//                 red.hitting();
//              } else {
//                 red.missing();
//              }

//              if (foundWhite) {
//                 white.hitting();
//              } else {
//                 white.missing();
//              }
//         }
//     }
// }
  public Map<Integer, int[]> calculateNumerosProximaCorProbabilidade(List<Roll> rolls) {
    Map<Integer, int[]> result = new HashMap<>();
    int[] hitsInit = {0,0,0};
    IntStream.range(0,15).forEach( i -> result.put(i, hitsInit));

    System.out.println(result);
    int galho = 2;

    for (int i=0;i < rolls.size()-galho;i++) {
        int[] hits = result.get(i).clone();
        List<Roll> galhos = rolls.subList(i+1, i+1+galho);
        boolean black = galhos.stream().anyMatch(roll -> Objects.equals(roll.getColor(), "black"));
        boolean red = galhos.stream().anyMatch(roll -> Objects.equals(roll.getColor(), "red"));
        boolean white = galhos.stream().anyMatch(roll -> Objects.equals(roll.getColor(), "white"));

        if (black) {
            hits[0] = hits[0]+1;
        }

        if (red) {
            hits[1] = hits[1]+1;
        }

        if (white) {
            hits[2] = hits[2]+1;
        }

        result.put(i, hits);
    }

     return result;
  }
}
