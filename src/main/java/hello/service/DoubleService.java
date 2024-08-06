package hello.service;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import hello.ExcelHelper;
import hello.Roll;
import hello.repository.DoubleRepository;

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
        System.out.println("creu "+platform);
        // return repository.findByPlatform(platform);
        Sort.Direction sortDirection = "asc".equals(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<Roll> page = repository.findByPlatform(platform, PageRequest.of(0, qtd, Sort.by(sortDirection, "created")));

  return page.get().collect(Collectors.toList());
    }

    public List<Roll> upload(MultipartFile file) {
        try {
            List<Roll> rolls = excelHelper.excelToRolls(file.getInputStream());
            _log.info("Qtd. Rolls = "+rolls.size());
            rolls.iterator().forEachRemaining(r -> _log.info(r.toString()));
            return rolls;
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }
    
}
