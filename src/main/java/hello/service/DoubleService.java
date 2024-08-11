package hello.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Service
public class DoubleService {

    private Logger _log = Logger.getLogger(getClass().getName());

    @Getter
    @Setter
    private List<Roll> rolls;

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

    public void upload(MultipartFile file) {
        try {
            rolls = excelHelper.excelToRolls(file.getInputStream());
            // rolls.iterator().forEachRemaining(r -> _log.info(r.toString()));
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public CoresPercentualDTO calculateCoresPercentual(int qtd) {
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
}
