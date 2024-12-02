package performanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

import com.sun.management.OperatingSystemMXBean;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.jupiter.api.*;
import javax.imageio.ImageIO;
import java.lang.management.ManagementFactory;
import java.io.File;
import java.io.IOException;


@TestMethodOrder(MethodOrderer.Random.class)

public class projectsPerformanceTests {

    private HttpClient client;
    private ObjectMapper objectMapper;


    private final String json = "application/json";
    private Response response;
    private String newProjectId;

    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final int[] targetSize = {1, 10, 100, 1000};

    private double[] timeStore = new double[targetSize.length];
    private double[] cpuUsageStore = new double[targetSize.length];
    private long[] freeMemoryStore = new long[targetSize.length];


    // Helper method to execute POST, PUT, DELETE requests
    private Response executeRequest(String method, String body, String url) {
        switch (method) {
            case "POST":
                return RestAssured.given().header("Content-Type", json).body(body).post(url);
            case "PUT":
                return RestAssured.given().header("Content-Type", json).body(body).put(url);
            case "DELETE":
                return RestAssured.given().delete(url);
            default:
                throw new IllegalArgumentException("Invalid method: " + method);
        }
    }

    // Helper method to track performance
    private void trackPerformance(int size, long startSampleTime,  double[] sampleTimeStore, double[] timeStore,
                                  double[] cpuUsageStore, long[] freeMemoryStore, int targetIndex, String body, String url, String method) {
        long start = System.nanoTime();
        
        response = executeRequest(method, body, url);
        
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
    }

    private void logPerformance(long startTime, long endTime, int targetIndex){
        long sampleTime = (endTime -  startTime) / 1_000_000;       // Convert to ms
        timeStore[targetIndex] = sampleTime;
        cpuUsageStore[targetIndex] = osBean.getCpuLoad() * 100;
        freeMemoryStore[targetIndex] = osBean.getFreeMemorySize() / (1024L * 1024L);
    }

    private void printStats(String operation) {
        System.out.println("\nPerformance Stats for " + operation + " Projects");
        System.out.println("______________________________________________________________________________");
        System.out.printf("| %-12s | %-18s | %-18s | %-18s |\n", "Size", "Time (ms)", "CPU Usage (%)", "Memory (MB)");
        System.out.println("|--------------|--------------------|--------------------|--------------------|");

        for (int i = 0; i < targetSize.length; i++) {
            System.out.printf("| %-12d | %-18.6f | %-18.2f | %-18d |\n",
                    targetSize[i],
                    timeStore[i],
                    cpuUsageStore[i],
                    freeMemoryStore[i]);
        }
        System.out.println("______________________________________________________________________________");

        // Generate Graph for Sample Time vs Category Size
        generateGraph(operation, targetSize, timeStore);
    }

    // Helper method to generate the graph using JFreeChart and save it as PNG
    private void generateGraph(String operation, int[] targetSize, double[] sampleTimeStore) {
        // Create a series of data points
        XYSeries series = new XYSeries("Sample Time");
        for (int i = 0; i < targetSize.length; i++) {
            series.add(targetSize[i], sampleTimeStore[i]);
        }

        // Create a dataset from the series
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                operation + " Projects - Time Taken vs Number of Projects",  // Chart title
                "Number of Projects",  // X-axis label
                "Time Taken (s)",  // Y-axis label
                dataset,  // Dataset
                PlotOrientation.VERTICAL,  // Plot orientation
                true,  // Legend
                true,  // Tooltips
                false
        );

        // Save the chart to a file
        saveChartToFile(chart, operation);
    }

    private void saveChartToFile(JFreeChart chart, String operation) {
        try {
            // Define the file path
            String directoryPath = "./src/test/java/performanceTests/graphs/";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();  // Create the directory if it doesn't exist
            }

            // Define the file name
            String fileName = operation + "_Projects_Time_vs_Size.png";
            File file = new File(directory, fileName);

            // Save the chart as a PNG image
            ImageIO.write(chart.createBufferedImage(800, 600), "PNG", file);
            System.out.println("Graph saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String createProjectAndReturnId() throws IOException, InterruptedException {
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
        newProjectId = id;

        return id;
    }

    private void deleteProjectById(String projectId) throws IOException, InterruptedException {
        //String id = createProjectAndReturnId();
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/" + projectId))
                .DELETE()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode(), "Failed to delete project with id: " + projectId);
    }

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        // Reset all stores
        timeStore = new double[targetSize.length];
        cpuUsageStore = new double[targetSize.length];
        freeMemoryStore = new long[targetSize.length];
    }


    @Test
    public void createProjectPerformance() throws IOException, InterruptedException {

        for (int i=0; i<targetSize.length; i++){
            long startSampleTime = System.nanoTime();
            String[] projectIDs = new String[targetSize[i]];
            for (int j=0; j<targetSize[i]; j++){

                String requestBody = "{ \"title\": \"s aute irure dolor i\", \"active\": false, \"completed\": false, \"description\": \"sse cillum dolore eu\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
        
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(201, response.statusCode());
                String responseBody = response.body();
                ObjectMapper objectMapper = new ObjectMapper();
                String id = objectMapper.readTree(responseBody).get("id").asText();
                projectIDs[j] = id;
            }
            // Record
            long endTime = System.nanoTime();
            logPerformance(startSampleTime, endTime, i);
            
            // Cleanup
            for (int k=0; k<projectIDs.length; k++){
                deleteProjectById(projectIDs[k]);
            }

        }
        printStats("Add");
    }



    /* 
    // Time to complete one update/change project operation
    @Test
    public void updateProjectPerformance() throws IOException, InterruptedException {

        for (int i=0; i<targetSize.length; i++){
            long startSampleTime = System.nanoTime();
            for (int j=0; j<targetSize[i]; j++){
                newProjectId = createProjectAndReturnId();
                String requestBody = "{ \"title\": \"Updated Title\", \"active\": true, \"completed\": false, \"description\": \"Updated Description\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects/" + newProjectId))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode());
            }
            long endTime = System.nanoTime();
            logPerformance(startSampleTime, endTime, i);
        }
        printStats("Change");
    }
        */

    /* 
    @Test
    public void deleteCategoryPerformance() {
        double[] sampleTimeStore = new double[targetSize.length];
        double[] timeStore = new double[targetSize.length];
        double[] cpuUsageStore = new double[targetSize.length];
        long[] freeMemoryStore = new long[targetSize.length];
        int targetIndex = 0;
        long startSampleTime = System.nanoTime();

        for (int i = 1; i <= targetSize[targetSize.length - 1]; i++) {
            String body = "{\"title\":\"" + i + "\",\"description\":\"\"}";
            String url = "http://localhost:4567/categories";
            response = executeRequest("POST", body, url);
            newProjectId = response.jsonPath().get("id");

            if (targetSize[targetIndex] == i) {
                url = "http://localhost:4567/categories/" + newProjectId;
                trackPerformance(i, startSampleTime, sampleTimeStore, timeStore, cpuUsageStore, freeMemoryStore, targetIndex, body, url, "DELETE");
                targetIndex++;
            }
        }

        printStats("Delete", timeStore, cpuUsageStore, freeMemoryStore, sampleTimeStore);
    }

    @Test
    public void changeCategoryPerformance() {
        double[] sampleTimeStore = new double[targetSize.length];
        double[] timeStore = new double[targetSize.length];
        double[] cpuUsageStore = new double[targetSize.length];
        long[] freeMemoryStore = new long[targetSize.length];
        int targetIndex = 0;
        long startSampleTime = System.nanoTime();

        for (int i = 1; i <= targetSize[targetSize.length - 1]; i++) {
            String body = "{\"title\":\"" + i + "\",\"description\":\"\"}";
            String url = "http://localhost:4567/categories";
            response = executeRequest("POST", body, url);
            newProjectId = response.jsonPath().get("id");

            if (targetSize[targetIndex] == i) {
                body = "{\"title\": \"new category\"}";
                url = "http://localhost:4567/categories/" + newProjectId;
                trackPerformance(i, startSampleTime, sampleTimeStore, timeStore, cpuUsageStore, freeMemoryStore, targetIndex, body, url, "PUT");
                targetIndex++;
            }
        }

        printStats("Change", timeStore, cpuUsageStore, freeMemoryStore, sampleTimeStore);
    }

*/






    /* 
    
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

    */


    
}
