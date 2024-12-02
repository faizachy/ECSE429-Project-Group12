package performanceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class projectPerformanceTestsForOneObject {

    private HttpClient client;
    private ObjectMapper objectMapper;

    public static String categoryId = "0";
    public static String taskId = "0";

    public static String projectId = "0";

    public static String createdProjectId = "0";

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        projectId = createProjectAndReturnId();
    }

    @AfterEach
    public void restore() throws IOException, InterruptedException {
        deleteProjectById(projectId);
    }

    private static String createProjectAndReturnId() throws IOException, InterruptedException {
        String projectRequestBody = "{ \"title\": \"Project Title\", \"active\": false, \"completed\": false, \"description\": \"Project Description\" }";

        HttpRequest projectRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(projectRequestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> projectResponse = client.send(projectRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, projectResponse.statusCode());

        String responseBody = projectResponse.body();
        String id = new ObjectMapper().readTree(responseBody).get("id").asText();

        return id;
    }

    private static void deleteProjectById(String projectId) throws IOException, InterruptedException {
        String id = createProjectAndReturnId();
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/" + id))
                .DELETE()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode(), "Failed to delete project with id: " + projectId);
    }


     // Time to complete one create project operation
    @Test
    public void testCreateProject() throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        String requestBody = "{ \"title\": \"s aute irure dolor i\", \"active\": false, \"completed\": false, \"description\": \"sse cillum dolore eu\" }";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        //System.out.println(response.body());

        String responseBody = response.body();
        ObjectMapper objectMapper = new ObjectMapper();
        String id = objectMapper.readTree(responseBody).get("id").asText();
        
        long endTime = System.nanoTime();
        long durationMillis = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        System.out.println("Time taken to create a project: " + durationMillis + " ms");

        deleteProjectById(id);
    }


    // Time to complete one update/change project operation
    @Test
    public void testUpdateProjectPerformance() throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        String requestBody = "{ \"title\": \"Updated Title\", \"active\": true, \"completed\": false, \"description\": \"Updated Description\" }";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/" + projectId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        //System.out.println(response.body());

        long endTime = System.nanoTime();
        long durationMillis = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        System.out.println("Time taken to update a project: " + durationMillis + " ms");
    }

    // Time to complete one delete project operation
    @Test
    public void testDeleteProject() throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/"+ projectId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
       // System.out.println(response.body());
        long endTime = System.nanoTime();
        long durationMillis = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        System.out.println("Time taken to delete a project: " + durationMillis + " ms");
    }
    
    
}
