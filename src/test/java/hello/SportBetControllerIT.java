package hello;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext
@AutoConfigureMockMvc
public class SportBetControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${theodds.api.base.url}")
    private String theOddsAiBaseUrl;

    @Autowired
    private MockMvc mockMvc;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldGetEVs() throws Exception {
        File filejson = ResourceUtils.getFile("classpath:get-upcoming-odds.json");

        JsonNode json = new ObjectMapper().readTree(filejson);

        long preMatchTime = Instant.now().plus(Duration.ofDays(2)).toEpochMilli()/1000;
        long liveTime = Instant.now().minus(Duration.ofHours(2)).toEpochMilli()/1000;

        mockServer
                .expect(MockRestRequestMatchers
                        .requestTo(new URI(
                                "https://creu.com/v4/sports/upcoming/odds?apiKey=creu&markets=h2h,spreads,totals&regions=eu,uk&dateFormat=unix")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(
                        json.toString().replace("prematch-date", Long.toString((preMatchTime)).replace("live-date", Long.toString(liveTime))),
                        MediaType.APPLICATION_JSON));

        // Perform the actual API request
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/sports/valuebet?bankroll=100&markets=h2h&minEV=1.3&minOdd=10&live=true"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(1)))
                .andExpect(jsonPath("$.[0].partida.id", Matchers.is("live-id")))
                .andExpect(jsonPath("$.[0].evs.length()", Matchers.is(1)))
                .andDo(MockMvcResultHandlers.print());

        mockServer.verify();

    }

    private String _formatDate(Instant time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        return time.atZone(ZoneId.systemDefault()).format(formatter);
    }
}