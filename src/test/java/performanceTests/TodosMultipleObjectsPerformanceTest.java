package performanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
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

public class TodosMultipleObjectsPerformanceTest {
    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final int[] targetSize = {1, 10, 50, 100, 250, 500, 1000};
    private final HttpClient client = HttpClient.newHttpClient();
    public static String categoryId = "0";
    public static String taskId = "0";
    public static String todoId = "0";
    private static ProcessBuilder pb;

    // Helper method to generate the graph using JFreeChart and save it as PNG
    private void generateGraph(String operation, int[] targetSize, double[] sampleTimeStore,
                               double[] cpuUsageMetrics, long [] availableMemoryMetrics) {
        // Create series of data points
        XYSeries time_series = new XYSeries("Cumulative Time");
        for (int i = 0; i < targetSize.length; i++) {
            time_series.add(targetSize[i], sampleTimeStore[i]);
        }

        XYSeries cpuUsage_series = new XYSeries("CPU Usage");
        for (int i = 0; i < targetSize.length; i++) {
            cpuUsage_series.add(targetSize[i], cpuUsageMetrics[i]);
        }

        XYSeries memory_series = new XYSeries("Memory Usage");
        for (int i = 0; i < targetSize.length; i++) {
            memory_series.add(targetSize[i], availableMemoryMetrics[i]);
        }

        // Create datasets from the series
        XYSeriesCollection time_dataset = new XYSeriesCollection(time_series);
        XYSeriesCollection cpu_dataset = new XYSeriesCollection(cpuUsage_series);
        XYSeriesCollection memory_dataset = new XYSeriesCollection(memory_series);

        // Create charts
        JFreeChart time_chart = ChartFactory.createXYLineChart(
                operation + " Todos - Cumulative Time vs Todo Size",
                "Todo Size",
                "Cumulative Time (s)",
                time_dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        JFreeChart cpu_chart = ChartFactory.createXYLineChart(
                operation + " Todos - CPU Usage vs Todo Size",
                "Todo Size",
                "CPU Usage (%)",
                cpu_dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        JFreeChart memory_chart = ChartFactory.createXYLineChart(
                operation + " Todos -Memory Usage vs Todo Size",
                "Todo Size",
                "Memory Usage",
                memory_dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Save the charts to files
        saveChartToFile(time_chart, operation, "_Todos_CumulativeTime_vs_Size.png");
        saveChartToFile(cpu_chart, operation, "_Todos_CPU_usage_vs_Size.png");
        saveChartToFile(memory_chart, operation, "_Todos_MemoryUsage_vs_Size.png");
    }

    private void saveChartToFile(JFreeChart chart, String operation, String filename) {
        try {
            // Define the file path
            String directoryPath = "./src/test/java/performanceTests/graphs/todos/";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();  // Create the directory if it doesn't exist
            }

            // Define the file name
            String fileName = operation + filename;
            File file = new File(directory, fileName);

            // Save the chart as a PNG image
            ImageIO.write(chart.createBufferedImage(800, 600), "PNG", file);
            System.out.println("Graph saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
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
        generateGraph("Add", targetSize, cumulativeTimeStore,cpuUsageMetrics, availableMemoryMetrics);
        System.out.println("Add Todos Statistics");
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
                String requestBody = "{ \"title\": \" " + currentSize + " \", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
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
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String requestBody = "{ \"title\": \" " + currentSize + " \", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
                HttpRequest request_create = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response_create = client.send(request_create, HttpResponse.BodyHandlers.ofString());
                String responseBody = response_create.body();

                // delete and verify
                String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                        .DELETE()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode());
            }
        }
        generateGraph("Delete", targetSize, cumulativeTimeStore, cpuUsageMetrics, availableMemoryMetrics);
        System.out.println("Delete Todos Statistics");
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
        generateGraph("Update", targetSize, cumulativeTimeStore, cpuUsageMetrics, availableMemoryMetrics);
        System.out.println("Update Todos Statistics");
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
