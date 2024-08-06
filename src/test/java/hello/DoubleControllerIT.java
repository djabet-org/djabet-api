package hello;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties")
public class DoubleControllerIT
 {

  @LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

      private Roll roll1 = Roll.builder()
      .color("red")
      .id(1L)
      .createdTime(Instant.now().toEpochMilli())
      .platform("blaze")
      .roll(5)
      .build();
      
      private Roll roll2 = Roll.builder()
      .color("black")
      .id(2L)
      .createdTime(Instant.now().plusSeconds(30).toEpochMilli())
      .platform("betfiery")
      .roll(6)
      .build();

      private Roll roll3 = Roll.builder()
      .color("black")
      .id(3L)
      .createdTime(Instant.now().plusSeconds(60).toEpochMilli())
      .platform("blaze")
      .roll(7)
      .build();

      @BeforeEach
      public void setup() {
        saveRoll(roll1);
        saveRoll(roll2);
        saveRoll(roll3);
      }

    @Test
    public void shouldSaveRoll() {

      Roll newRoll = Roll.builder()
      .color("red")
      .id(1L)
      .platform("chillbet")
      .roll(5)
      .build();

      ResponseEntity<String> response = saveRoll(newRoll);
      assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

    }

    @Test
    public void shouldGetRollsFilter() throws JsonMappingException, JsonProcessingException {
      ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/double?platform=blaze&qtd=2&sort=desc", String.class);
      List<Roll> rolls = new ObjectMapper().readValue(response.getBody(), new TypeReference<List<Roll>>(){});
      System.out.println("hey "+rolls);

      assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
      assertEquals(2, rolls.size());
      assertEquals(3, rolls.get(0).getId());
      assertEquals("blaze", rolls.get(0).getPlatform());
      assertEquals(1, rolls.get(1).getId());
      assertEquals("blaze", rolls.get(1).getPlatform());
    }

@Test
public void throw400BadRequest_WhenNull() {
      Roll newRoll = Roll.builder()
      .color("red")
      .build();

      ResponseEntity<String> response = saveRoll(newRoll);

      assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
}

@AfterEach
public void teardown() {
}

private ResponseEntity<String> saveRoll(Roll roll) {
       HttpEntity<Roll> request = new HttpEntity<>(roll, null);
      return this.restTemplate.postForEntity("http://localhost:" + port + "/api/double", request, String.class);

}
}