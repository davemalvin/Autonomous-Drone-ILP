package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class JsonParser {
    
    // Create a new HttpClient with default settings
    public static final HttpClient client = HttpClient.newHttpClient();
    
    public static String readJson(String urlString) throws IOException, InterruptedException {
        // Build an HTTP GET request and send it to the HTTP client
        var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
        var response = client.send(request, BodyHandlers.ofString());
            
        // Get the content as a string
        var json = response.body();
        return json;
    }
}
