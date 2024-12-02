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
    private final int[] targetSize = {1, 10, 50, 100, 250, 500, 1000};
    private final HttpClient client = HttpClient.newHttpClient();
    public static String categoryId = "0";
    public static String taskId = "0";
    public static String todoId = "0";
    private static ProcessBuilder pb;
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
                .uri(URI.create("http://localhost:4567/todos/" + todoId + "/categories"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(linkBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        return id;
    }

    @Test
    public void testCreateTodo() throws IOException, InterruptedException {
        long testStartTime = System.nanoTime();
        double[] cumulativeTimeStore = new double[targetSize.length];
        double[] operationTimeStore = new double[targetSize.length];
        double[] cpuUsageMetrics = new double[targetSize.length];
        long[] availableMemoryMetrics = new long[targetSize.length];
        int sizeIndex = 0;

        for (int currentSize = 1; currentSize <= targetSize[targetSize.length - 1]; currentSize++) {
            if (targetSize[sizeIndex] == currentSize) {
                long operationStartTime = System.nanoTime();
                String requestBody = "{ \"title\": " + currentSize + ", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                long operationEndTime = System.nanoTime();
                long elapsedTestTime = operationEndTime - testStartTime;
                double elapsedTestTimeInSeconds = (double) elapsedTestTime / 1_000_000_000;
                cumulativeTimeStore[sizeIndex] = elapsedTestTimeInSeconds;

                long operationDuration = operationEndTime - operationStartTime;
                double operationDurationInSeconds = (double) operationDuration / 1_000_000_000;
                double cpuUsagePercentage = osBean.getCpuLoad() * 100;
                long freeMemoryMB = osBean.getFreeMemorySize() / (1024L * 1024L);

                operationTimeStore[sizeIndex] = operationDurationInSeconds;
                cpuUsageMetrics[sizeIndex] = cpuUsagePercentage;
                availableMemoryMetrics[sizeIndex] = freeMemoryMB;

                sizeIndex++;

                // Delete the created object
                String todoIdToDelete = new ObjectMapper().readTree(responseBody).get("id").asText(); // Renamed from todoId_delete
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos/" + todoIdToDelete))
                        .DELETE()
                        .build();
                HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                assertEquals(201, response.statusCode());
            } else {
                String requestBody = "{ \"title\": " + currentSize + ", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                // Delete the created object
                String todoIdToDelete = new ObjectMapper().readTree(responseBody).get("id").asText();
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos/" + todoIdToDelete))
                        .DELETE()
                        .build();
                HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                assertEquals(201, response.statusCode());
            }
        }

        System.out.println("Add Todos Statistics\n");
        System.out.printf("%-10s %-20s %-20s %-20s %-20s%n", "SIZE", "TIME (s)", "CPU USAGE (%)", "MEMORY (MB)", "CUMULATIVE TIME (s)");
        for (int i = 0; i < targetSize.length; i++) {
            System.out.printf("%-10d %-20f %-20f %-20d %-20f%n",
                    targetSize[i],
                    operationTimeStore[i],
                    cpuUsageMetrics[i],
                    availableMemoryMetrics[i],
                    cumulativeTimeStore[i]);
        }
    }

    @Test
    public void testDeleteTodo() throws IOException, InterruptedException {
        long testStartTime = System.nanoTime();
        double[] cumulativeTimeStore = new double[targetSize.length];
        double[] operationTimeStore = new double[targetSize.length];
        double[] cpuUsageMetrics = new double[targetSize.length];
        long[] availableMemoryMetrics = new long[targetSize.length];
        int sizeIndex = 0;

        for (int currentSize = 1; currentSize <= targetSize[targetSize.length - 1]; currentSize++) {
            if (targetSize[sizeIndex] == currentSize) {

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

                long operationStartTime = System.nanoTime();

                // delete and verify
                long startTime = System.nanoTime();
                String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                        .DELETE()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                long operationEndTime = System.nanoTime();
                long elapsedTestTime = operationEndTime - testStartTime;
                double elapsedTestTimeInSeconds = (double) elapsedTestTime / 1_000_000_000;
                cumulativeTimeStore[sizeIndex] = elapsedTestTimeInSeconds;

                long operationDuration = operationEndTime - operationStartTime;
                double operationDurationInSeconds = (double) operationDuration / 1_000_000_000;
                double cpuUsagePercentage = osBean.getCpuLoad() * 100;
                long freeMemoryMB = osBean.getFreeMemorySize() / (1024L * 1024L);

                operationTimeStore[sizeIndex] = operationDurationInSeconds;
                cpuUsageMetrics[sizeIndex] = cpuUsagePercentage;
                availableMemoryMetrics[sizeIndex] = freeMemoryMB;

                sizeIndex++;
                assertEquals(200, response.statusCode());
            } else {
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
            }
        }

        System.out.println("Delete Todos Statistics\n");
        System.out.printf("%-10s %-20s %-20s %-20s %-20s%n", "SIZE", "TIME (s)", "CPU USAGE (%)", "MEMORY (MB)", "CUMULATIVE TIME (s)");
        for (int i = 0; i < targetSize.length; i++) {
            System.out.printf("%-10d %-20f %-20f %-20d %-20f%n",
                    targetSize[i],
                    operationTimeStore[i],
                    cpuUsageMetrics[i],
                    availableMemoryMetrics[i],
                    cumulativeTimeStore[i]);
        }
    }


    @Test
    public void testUpdateTodoWithAllFields() throws IOException, InterruptedException {
        long testStartTime = System.nanoTime();
        double[] cumulativeTimeStore = new double[targetSize.length];
        double[] operationTimeStore = new double[targetSize.length];
        double[] cpuUsageMetrics = new double[targetSize.length];
        long[] availableMemoryMetrics = new long[targetSize.length];
        int sizeIndex = 0;

        for (int currentSize = 1; currentSize <= targetSize[targetSize.length - 1]; currentSize++) {
            if (targetSize[sizeIndex] == currentSize) {
                long operationStartTime = System.nanoTime();
                String requestBody = "{ \"title\": \"Updated Title\", \"doneStatus\": false, \"description\": \"Updated Description\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos/" + todoId))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long operationEndTime = System.nanoTime();
                long elapsedTestTime = operationEndTime - testStartTime;
                double elapsedTestTimeInSeconds = (double) elapsedTestTime / 1_000_000_000;
                cumulativeTimeStore[sizeIndex] = elapsedTestTimeInSeconds;

                long operationDuration = operationEndTime - operationStartTime;
                double operationDurationInSeconds = (double) operationDuration / 1_000_000_000;
                double cpuUsagePercentage = osBean.getCpuLoad() * 100;
                long freeMemoryMB = osBean.getFreeMemorySize() / (1024L * 1024L);

                operationTimeStore[sizeIndex] = operationDurationInSeconds;
                cpuUsageMetrics[sizeIndex] = cpuUsagePercentage;
                availableMemoryMetrics[sizeIndex] = freeMemoryMB;

                sizeIndex++;
                assertEquals(200, response.statusCode());

            } else {
                String requestBody = "{ \"title\": \"Updated Title\", \"doneStatus\": false, \"description\": \"Updated Description\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos/" + todoId))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode());
            }
        }

        System.out.println("Update Todos Statistics\n");
        System.out.printf("%-10s %-20s %-20s %-20s %-20s%n", "SIZE", "TIME (s)", "CPU USAGE (%)", "MEMORY (MB)", "CUMULATIVE TIME (s)");
        for (int i = 0; i < targetSize.length; i++) {
            System.out.printf("%-10d %-20f %-20f %-20d %-20f%n",
                    targetSize[i],
                    operationTimeStore[i],
                    cpuUsageMetrics[i],
                    availableMemoryMetrics[i],
                    cumulativeTimeStore[i]);
        }
    }
}
