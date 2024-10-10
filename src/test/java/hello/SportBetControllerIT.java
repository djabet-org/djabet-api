package hello;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URI;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
        mockServer = MockRestServiceServer.createServer(this.restTemplate);
    }

    @Test
    public void shouldGetEVs() throws Exception {
        File filejson = ResourceUtils.getFile("classpath:get-upcoming-odds.json");

        JsonNode json = new ObjectMapper().readTree(filejson);
        // System.out.println(json);
        mockServer
                .expect(MockRestRequestMatchers
                        .requestTo(new URI("https://creu.com/v4/sports/upcoming/odds?apiKey=creu&markets=h2h,spreads,totals&regions=eu,uk")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(json.toString(), MediaType.APPLICATION_JSON)
                        );

                        // Perform the actual API request
        mockMvc.perform(MockMvcRequestBuilders.get("/api/sports/valuebet?bankroll=100"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\"data\": \"mock data\"}"));

        ResponseEntity<String> response = _getEvs();
        // mockServer.verify();

        JsonNode responseBody = new ObjectMapper().readTree(response.getBody());
            assertEquals(responseBody.isArray(), true);

    }

    @AfterEach
    public void teardown() {
    }

    private ResponseEntity<String> _getEvs() {
        return this.restTemplate.getForEntity("/api/sports/valuebet?bankroll=100",
                String.class);

    }
}