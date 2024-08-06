package hello.unit;

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
public void shouldCallGetRollsService() throws JsonProcessingException {
    when(service.fetch(2, "asc", "blaze")).thenReturn(
        List.of(
            Roll.builder().createdTime(Instant.now().toEpochMilli()).build(),
            Roll.builder().createdTime(Instant.now().plusSeconds(60).toEpochMilli()).build()
        ));

    controller.fetchRolls(
        Optional.of(2),
        Optional.of("asc"),
        Optional.of("blaze"));

    verify(service).fetch(2,"asc", "blaze");
}

@Test
public void shouldCallSeeService() {
    Roll roll = mock(Roll.class);
    controller.saveRoll(roll, null);

    verify(sseService).sendEvents(roll);
}

}