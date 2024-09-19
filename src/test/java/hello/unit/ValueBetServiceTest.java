package hello.unit;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hello.repository.TheOddsAPI;
import hello.service.ValueBet;
import hello.service.ValueBetService;

@ExtendWith(MockitoExtension.class)
public class ValueBetServiceTest {
    
    @Mock
    private TheOddsAPI theOddsAPI;

    @Test
    public void testGetValueBets() {
        ValueBetService valueBetService = new ValueBetService();

        List<ValueBet> result = valueBetService.getValueBets();

        verify(theOddsAPI).getUpcomingPartidas();
    }
}
