import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

    @TestMethodOrder(MethodOrderer.Random.class)
    public class TodosPerformanceTest {

        private final HttpClient client = HttpClient.newHttpClient();
        public static String categoryId = "0";
        public static String taskId = "0";
        public static String todoId = "0";
        @BeforeEach
        public void setup_foreach() throws IOException, InterruptedException {
            String requestBody = "{ \"title\": \"s aute irure dolor i\", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            String responseBody = response.body();
            todoId = new ObjectMapper().readTree(responseBody).get("id").asText();
            taskId = createTaskOfTodo();
            categoryId = createCategory();
        }

        @AfterEach
        public void teardown() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
        }
        public static String createTaskOfTodo() throws IOException, InterruptedException {
            // Initialize the HttpClient
            HttpClient client = HttpClient.newHttpClient();

            String requestBody = "{ \"Id\": \"1\" }";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/tasksof"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode());

            return "1";
        }

        public static String createCategory() throws IOException, InterruptedException {
            // Initialize the HttpClient
            HttpClient client = HttpClient.newHttpClient();
            String categoryRequestBody = "{ \"title\": \"Category Title\", \"description\": \"Category Description\" }";
            HttpRequest categoryRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/categories"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(categoryRequestBody))
                    .build();

            HttpResponse<String> categoryResponse = client.send(categoryRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, categoryResponse.statusCode());

            // Extract the category ID from the response body
            String responseBody = categoryResponse.body();
            String id = new ObjectMapper().readTree(responseBody).get("id").asText();

            String linkBody = "{ \"Id\": \"" + id + "\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" +todoId +"/categories"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(linkBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            return id;
        }

        @Test
        public void testCreateTodo() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"s aute irure dolor i\", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();


            long endTime = System.nanoTime();

            // delete the created object
            String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
            HttpRequest request_delete = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                    .DELETE()
                    .build();

            HttpResponse<String> response_delete = client.send(request_delete, HttpResponse.BodyHandlers.ofString());
            // verify creation

            System.out.println("Time taken to create todo: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(201, response.statusCode());
        }
        @Test
        public void testDeleteTodo() throws IOException, InterruptedException {

            //create an object first
            String requestBody = "{ \"title\": \"s aute irure dolor i\", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
            HttpRequest request_create = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response_create = client.send(request_create, HttpResponse.BodyHandlers.ofString());
            String responseBody = response_create.body();
            long startTime = System.nanoTime();
            // delete and verify
            String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to delete todo: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(200, response.statusCode());
        }

        @Test
        public void testUpdateTodoWithAllFields() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"Updated Title\", \"doneStatus\": false, \"description\": \"Updated Description\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to update todo: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(200, response.statusCode());

        }


    }

