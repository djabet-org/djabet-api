package hello.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpClient {
    
    public JsonNode get(String url) throws IOException {

// URL url = new URL("https://api.the-odds-api.com/v4/sports/icehockey_nhl/odds/?apiKey=d70d303fba985cbfa62b6cc49c255604&regions=eu");
URL urlURL = new URL(url);

// Open a connection(?) on the URL(??) and cast the response(???)
HttpURLConnection connection = (HttpURLConnection) urlURL.openConnection();

// Now it's "open", we can set the request method, headers etc.
connection.setRequestProperty("accept", "application/json");

// This line makes the request
InputStream responseStream = connection.getInputStream();

// Manually converting the response body InputStream to APOD using Jackson
ObjectMapper mapper = new ObjectMapper();
return mapper.readTree(responseStream);
    }
}
