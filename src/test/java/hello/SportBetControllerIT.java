package hello;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Value;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties")
public class SportBetControllerIT
 {

  @LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

    @Value("${theodds.api.base.url}")
    private String theOddsAiBaseUrl;

    private MockRestServiceServer mockServer;
      public void setup() {
                mockServer = MockRestServiceServer.createServer(this.restTemplate);
      }

    @Test
    public void shouldGetEVs() throws JsonMappingException, JsonProcessingException {

      ResponseEntity<String> response = _getEvs();
      JsonNode responseBody = new ObjectMapper().readTree(response.getBody());
      assertEquals(responseBody.isArray(), true);

    }


@AfterEach
public void teardown() {
}

private ResponseEntity<String> _getEvs() {
      return this.restTemplate.getForEntity("http://localhost:" + port + "/api/sports/valuebet?bankroll=100", String.class);

}
}