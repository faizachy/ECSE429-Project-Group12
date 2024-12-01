import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
//    @TestMethodOrder(MethodOrderer.Random.class)
    public class TodosMultipleObjectsPerformanceTest {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        private static ProcessBuilder pb;
        private final int[] targetSize = {1, 10, 50, 100, 250, 500, 1000};
        private final HttpClient client = HttpClient.newHttpClient();
        public static String categoryId = "0";
        public static String taskId = "0";
        public static String todoId = "0";
    @BeforeAll
    static void setupProcess() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) {
            pb = new ProcessBuilder(
                    "cmd.exe", "/c", "java -jar .\\src\\test\\resources\\runTodoManagerRestAPI-1.5.5.jar");
        }
        else {
            pb = new ProcessBuilder(
                    "sh", "-c", "java -jar ./src/test/resources/runTodoManagerRestAPI-1.5.5.jar");
        }
    }

    @BeforeEach
    void startServer() throws InterruptedException {
        try {
            pb.start();
            Thread.sleep(1000);
        } catch (IOException e) {
            System.out.println("No server");
        }
    }

    @AfterEach
    void shutServer() {
        try {
            RestAssured.get("http://localhost:4567/shutdown");
        }
        catch (Exception ignored) {
        }
    }
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
            long startSampleTime = System.nanoTime();
            double[] sampleTimeStore = new double[targetSize.length];
            double[] timeStore = new double[targetSize.length];
            double[] cpuUsageStore = new double[targetSize.length];
            long[] freeMemoryStore = new long[targetSize.length];
            int targetIndex = 0;
            for (int i = 1; i <= targetSize[targetSize.length - 1]; i++) {
                if (targetSize[targetIndex] == i) {
                    long start = System.nanoTime();
                    String requestBody = "{ \"title\": " + i + ", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:4567/todos"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String responseBody = response.body();
                    long finish = System.nanoTime();
                    long sampleTimeElapsed = finish - startSampleTime;
                    double sampleTimeElapsedInSeconds = (double) sampleTimeElapsed / 1_000_000_000;
                    sampleTimeStore[targetIndex] = sampleTimeElapsedInSeconds;
                    long timeElapsed = finish - start;
                    double elapsedTimeInSecond = (double) timeElapsed / 1_000_000_000;
                    double cpuUsage = osBean.getCpuLoad() * 100;
                    long memory = osBean.getFreeMemorySize() / (1024L * 1024L);
                    timeStore[targetIndex] = elapsedTimeInSecond;
                    cpuUsageStore[targetIndex] = cpuUsage;
                    freeMemoryStore[targetIndex] = memory;
                    targetIndex++;
                    // delete the created object
                    String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
                    HttpRequest request_delete = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                            .DELETE()
                            .build();

                    HttpResponse<String> response_delete = client.send(request_delete, HttpResponse.BodyHandlers.ofString());
                    assertEquals(201, response.statusCode());
                }
                else {
                        String requestBody = "{ \"title\": " + i + ", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:4567/todos"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        String responseBody = response.body();
                    // delete the created object
                    String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
                    HttpRequest request_delete = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                            .DELETE()
                            .build();

                    HttpResponse<String> response_delete = client.send(request_delete, HttpResponse.BodyHandlers.ofString());
                    assertEquals(201, response.statusCode());
                    }

        }

            System.out.println("-Add Todos Statistics\n");
            System.out.printf("%-10s %-20s %-20s %-20s %-20s%n", "SIZE", "TIME (s)", "CPU USAGE (%)", "MEMORY (MB)", "Sample Time (s)");
            for (int i = 0; i < targetSize.length; i++) {
                System.out.printf("%-10d %-20f %-20f %-20d %-20f%n",
                        targetSize[i],
                        timeStore[i],
                        cpuUsageStore[i],
                        freeMemoryStore[i],
                        sampleTimeStore[i]);
            }
        }
        @Test
        public void testDeleteTodo() throws IOException, InterruptedException {
            for (int todoCount = 10; todoCount <= 1000; todoCount *= 10) {
        long totalDuration = 0;

        for (int i = 0; i < todoCount; i++) {

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

            // delete and verify
            long startTime = System.nanoTime();
            String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }

        System.out.println("Average time for " + todoCount + " todos: " + (totalDuration / todoCount) / 1_000_000 + " ms");
    }

        }



        @Test
        public void testUpdateTodoWithID() throws IOException, InterruptedException {
            for (int todoCount = 10; todoCount <= 1000; todoCount *= 10) {
        long totalDuration = 0;

        for (int i = 0; i < todoCount; i++) {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \" new title \", \"doneStatus\": false, \"description\": \"new description\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody)) // Empty body
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }

        System.out.println("Average time for " + todoCount + " todos: " + (totalDuration / todoCount) / 1_000_000 + " ms");
    }
        }



        @Test
        public void testUpdateTodoWithAllFields() throws IOException, InterruptedException {
            for (int todoCount = 10; todoCount <= 1000; todoCount *= 10) {
        long totalDuration = 0;

        for (int i = 0; i < todoCount; i++) {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"Updated Title\", \"doneStatus\": false, \"description\": \"Updated Description\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }

        System.out.println("Average time for " + todoCount + " todos: " + (totalDuration / todoCount) / 1_000_000 + " ms");
    }

        }

        @Test
        public void testUpdateTodoTitle() throws IOException, InterruptedException {
            for (int todoCount = 10; todoCount <= 1000; todoCount *= 10) {
        long totalDuration = 0;

        for (int i = 0; i < todoCount; i++) {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"New Title\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }
        System.out.println("Average time for " + todoCount + " todos: " + (totalDuration / todoCount) / 1_000_000 + " ms");
    }
        }




        @Test
        public void testCreateLinkBetweenTodoAndCategory() throws IOException, InterruptedException {
            for (int todoCount = 10; todoCount <= 1000; todoCount *= 10) {
        long totalDuration = 0;

        for (int i = 0; i < todoCount; i++) {
            long startTime = System.nanoTime();
            String requestBody = "{ \"Id\": \"" + categoryId + "\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/categories"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }

        System.out.println("Average time for " + todoCount + " todos: " + (totalDuration / todoCount) / 1_000_000 + " ms");
    }

        }


        @Test
        public void testDeleteLinkBetweenTodoAndCategory() throws IOException, InterruptedException {
            for (int todoCount = 10; todoCount <= 1000; todoCount *= 10) {
        long totalDuration = 0;

        for (int i = 0; i < todoCount; i++) {
            long startTime = System.nanoTime();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/categories/" + categoryId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }

        System.out.println("Average time for " + todoCount + " todos: " + (totalDuration / todoCount) / 1_000_000 + " ms");
    }


        }

        @Test
        public void testCreateNewTaskOfTodo() throws IOException, InterruptedException {
            for (int todoCount = 10; todoCount <= 1000; todoCount *= 10) {
        long totalDuration = 0;

        for (int i = 0; i < todoCount; i++) {
            long startTime = System.nanoTime();
            String requestBody = "{ \"Id\": \"1\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/tasksof"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }

        System.out.println("Average time for " + todoCount + " todos: " + (totalDuration / todoCount) / 1_000_000 + " ms");
    }

        }

        @Test
        public void testDeleteTaskOfTodo() throws IOException, InterruptedException {
            for (int todoCount = 10; todoCount <= 1000; todoCount *= 10) {
        long totalDuration = 0;

        for (int i = 0; i < todoCount; i++) {
            long startTime = System.nanoTime();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/tasksof/1"))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            long endTime = System.nanoTime();
            totalDuration += (endTime - startTime);
        }

        System.out.println("Average time for " + todoCount + " todos: " + (totalDuration / todoCount) / 1_000_000 + " ms");
    }

        }
    }

