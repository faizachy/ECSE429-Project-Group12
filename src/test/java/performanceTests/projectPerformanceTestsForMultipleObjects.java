package performanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class projectPerformanceTestsForMultipleObjects {

    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final int[] targetSize = {1, 10, 50, 100, 250, 500, 1000};
    private final HttpClient client = HttpClient.newHttpClient();
    public static String projectId = "0";
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
        String requestBody = "{ \"title\": \"Project Title\", \"active\": false, \"completed\": false, \"description\": \"Project Description\" }";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        String responseBody = response.body();
        projectId = new ObjectMapper().readTree(responseBody).get("id").asText();
    }

    @AfterEach
    public void teardown() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/" + projectId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    public static String createTaskOfproject() throws IOException, InterruptedException {
        // Initialize the HttpClient
        HttpClient client = HttpClient.newHttpClient();

        String requestBody = "{ \"Id\": \"1\" }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/projects/" + projectId + "/tasksof"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        return "1";
    }

    // Helper method to generate the graph for time using JFreeChart and save it as PNG
    private void generateGraphForTime(String operation, int[] targetSize, double[] sampleTimeStore) {
        // Create a series of data points
        XYSeries series = new XYSeries("Sample Time");
        for (int i = 0; i < targetSize.length; i++) {
            series.add(targetSize[i], sampleTimeStore[i]);
        }

        // Create a dataset from the series
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                operation + " Projects - Number of Projects vs Time Taken",  // Chart title
                "Number of Projects",  // X-axis label
                "Time Taken (s)",  // Y-axis label
                dataset,  // Dataset
                PlotOrientation.VERTICAL,  // Plot orientation
                true,  // Legend
                true,  // Tooltips
                false
        );

        // Save the chart to a file
        saveChartToFile(chart, operation, "TIME");
    }

    // Helper method to generate the graph for time using JFreeChart and save it as PNG
    private void generateGraphForMemory(String operation, int[] targetSize, long[] availableMemoryMetrics) {
        // Create a series of data points
        XYSeries series = new XYSeries("Memory");
        for (int i = 0; i < targetSize.length; i++) {
            series.add(targetSize[i], availableMemoryMetrics[i]);
        }

        // Create a dataset from the series
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                operation + " Projects - Number of Projects vs Available Memory",  // Chart title
                "Number of Projects",  // X-axis label
                "Available Memory (MB)",  // Y-axis label
                dataset,  // Dataset
                PlotOrientation.VERTICAL,  // Plot orientation
                true,  // Legend
                true,  // Tooltips
                false
        );

        // Save the chart to a file
        saveChartToFile(chart, operation, "MEMORY");
    }

    private void generateGraphForCPU(String operation, int[] targetSize, double[] cpuUsageMetrics) {
        // Create a series of data points
        XYSeries series = new XYSeries("CPU Usage");
        for (int i = 0; i < targetSize.length; i++) {
            series.add(targetSize[i], cpuUsageMetrics[i]);
        }

        // Create a dataset from the series
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                operation + " Projects - Number of Projects vs CPU Usage",  // Chart title
                "Number of Projects",  // X-axis label
                "CPU Usage (%)",  // Y-axis label
                dataset,  // Dataset
                PlotOrientation.VERTICAL,  // Plot orientation
                true,  // Legend
                true,  // Tooltips
                false
        );

        // Save the chart to a file
        saveChartToFile(chart, operation, "CPU");
    }

    private void saveChartToFile(JFreeChart chart, String operation, String type) {
        try {
            // Define the file path
            String directoryPath = "./src/test/java/performanceTests/graphs/";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();  // Create the directory if it doesn't exist
            }
            String fileName = "";

            // Define the file name
            if (type.equalsIgnoreCase("MEMORY")){
                fileName = operation + "_Projects_Memory_vs_Size.png";
            }
            else if (type.equalsIgnoreCase("TIME")){
                fileName = operation + "_Projects_Time_vs_Size.png";
            }
            else{
                fileName = operation + "_Projects_CPU_vs_Size.png";
            }

            File file = new File(directory, fileName);

            // Save the chart as a PNG image
            ImageIO.write(chart.createBufferedImage(800, 600), "PNG", file);
            System.out.println("Graph saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testCreateProjectPerformance() throws IOException, InterruptedException {
        long testStartTime = System.nanoTime();
        double[] cumulativeTimeStore = new double[targetSize.length];
        double[] operationTimeStore = new double[targetSize.length];
        double[] cpuUsageMetrics = new double[targetSize.length];
        long[] availableMemoryMetrics = new long[targetSize.length];
        int sizeIndex = 0;

        for (int currentSize = 1; currentSize <= targetSize[targetSize.length - 1]; currentSize++) {
            if (targetSize[sizeIndex] == currentSize) {

                long operationStartTime = System.nanoTime();
                String requestBody = "{ \"title\": \"Project Title\", \"active\": false, \"completed\": false, \"description\": \"Project Description\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects"))
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
                String projectIdToDelete = new ObjectMapper().readTree(responseBody).get("id").asText();
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects/" + projectIdToDelete))
                        .DELETE()
                        .build();
                HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                assertEquals(201, response.statusCode());

                generateGraphForTime("Add", targetSize, cumulativeTimeStore);
                generateGraphForMemory("Add", targetSize, availableMemoryMetrics);
                generateGraphForCPU("Add", targetSize, cpuUsageMetrics);
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String requestBody = "{ \"title\": \"Project Title\", \"active\": false, \"completed\": false, \"description\": \"Project Description\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                // Delete the created object
                String projectIdToDelete = new ObjectMapper().readTree(responseBody).get("id").asText();
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects/" + projectIdToDelete))
                        .DELETE()
                        .build();
                HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
                assertEquals(201, response.statusCode());
            }
        }

        System.out.println("Add Projects Statistics");
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
    public void testDeleteprojectPerformance() throws IOException, InterruptedException {
        long testStartTime = System.nanoTime();
        double[] cumulativeTimeStore = new double[targetSize.length];
        double[] operationTimeStore = new double[targetSize.length];
        double[] cpuUsageMetrics = new double[targetSize.length];
        long[] availableMemoryMetrics = new long[targetSize.length];
        int sizeIndex = 0;

        for (int currentSize = 1; currentSize <= targetSize[targetSize.length - 1]; currentSize++) {
            if (targetSize[sizeIndex] == currentSize) {

                //create an object first
                String requestBody = "{ \"title\": \"Project Title\", \"active\": false, \"completed\": false, \"description\": \"Project Description\" }";
                HttpRequest request_create = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response_create = client.send(request_create, HttpResponse.BodyHandlers.ofString());
                String responseBody = response_create.body();

                long operationStartTime = System.nanoTime();

                // delete and verify
                String projectId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects/" + projectId_delete))
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
                generateGraphForTime("Delete", targetSize, cumulativeTimeStore);
                generateGraphForMemory("Delete", targetSize, availableMemoryMetrics);
                generateGraphForCPU("Delete", targetSize, cpuUsageMetrics);
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String requestBody = "{ \"title\": \"Project Title\", \"active\": false, \"completed\": false, \"description\": \"Project Description\" }";
                HttpRequest request_create = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response_create = client.send(request_create, HttpResponse.BodyHandlers.ofString());
                String responseBody = response_create.body();

                // delete and verify
                String projectId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects/" + projectId_delete))
                        .DELETE()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode());
            }
        }

        System.out.println("Delete Projects Statistics");
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
    public void testUpdateProjectPerformance() throws IOException, InterruptedException {
        long testStartTime = System.nanoTime();
        double[] cumulativeTimeStore = new double[targetSize.length];
        double[] operationTimeStore = new double[targetSize.length];
        double[] cpuUsageMetrics = new double[targetSize.length];
        long[] availableMemoryMetrics = new long[targetSize.length];
        int sizeIndex = 0;

        for (int currentSize = 1; currentSize <= targetSize[targetSize.length - 1]; currentSize++) {
            if (targetSize[sizeIndex] == currentSize) {
                long operationStartTime = System.nanoTime();
                String requestBody = "{ \"title\": \"Updated Title\", \"active\": true, \"completed\": false, \"description\": \"Updated Description\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects/" + projectId))
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
                generateGraphForTime("Update", targetSize, cumulativeTimeStore);
                generateGraphForMemory("Update", targetSize, availableMemoryMetrics);
                generateGraphForCPU("Update", targetSize, cpuUsageMetrics);

            } else {
                String requestBody = "{ \"title\": \"Updated Title\", \"active\": true, \"completed\": false, \"description\": \"Updated Description\" }";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/projects/" + projectId))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode());
            }
        }

        System.out.println("Update Projects Statistics");
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

