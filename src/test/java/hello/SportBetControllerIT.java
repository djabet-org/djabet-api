package hello;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@DirtiesContext
public class SportBetControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${theodds.api.base.url}")
    private String theOddsAiBaseUrl;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setup() {
        mockServer = MockRestServiceServer.createServer(this.restTemplate);
    }

    @Test
    public void shouldGetEVs() throws JsonMappingException, JsonProcessingException, URISyntaxException {
        mockServer
                .expect(MockRestRequestMatchers
                        .requestTo(new URI("http://localhost:" + port + "/api/sports/valuebet?bankroll=100")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("{\"abc\":1}", MediaType.APPLICATION_JSON)
                        );

        ResponseEntity<String> response = _getEvs();
        System.out.println(response.getBody());
        mockServer.verify();

        // JsonNode responseBody = new ObjectMapper().readTree(response.getBody());
        // assertEquals(responseBody.isArray(), true);

    }

    @AfterEach
    public void teardown() {
    }

    private ResponseEntity<String> _getEvs() {
        return this.restTemplate.getForEntity("http://localhost:" + port + "/api/sports/valuebet?bankroll=100",
                String.class);

    }
}