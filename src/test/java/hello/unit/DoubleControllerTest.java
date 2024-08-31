package hello.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonProcessingException;

import hello.DoubleController;
import hello.Roll;
import hello.data.CoresPercentualDTO;
import hello.data.DashboardDTO;
import hello.service.DoubleService;
import hello.service.SseService;

@ExtendWith(MockitoExtension.class)
public class DoubleControllerTest {

@InjectMocks
private DoubleController controller;

@Mock
private SseService sseService;
@Mock
private DoubleService service;

@Test
public void shouldCallServiceForCreation() {
    Roll roll = mock(Roll.class);
    controller.saveRoll(roll, null);

    verify(service).save(roll);
}

@Test
public void shouldCallGetDashboardContagemCores() throws JsonProcessingException {
    when(service.calculateCoresPercentual(anyList())).thenReturn(
        CoresPercentualDTO.builder().red(1).black(2).white(3).build());

    DashboardDTO result = controller.getDashboard(Optional.of(2));
    
    assertEquals(1, result.getCoresPercentualDTO().getRed());
    assertEquals(2, result.getCoresPercentualDTO().getBlack());
    assertEquals(3, result.getCoresPercentualDTO().getWhite());
    verify(service).calculateCoresPercentual(anyList());
}

@Test
public void shouldCallSeeService() {
    Roll roll = mock(Roll.class);
    controller.saveRoll(roll, null);

    verify(sseService).sendEvents(roll);
}

}