package performanceTests;

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

public class categoriesPerformance {

    private final String json = "application/json";
    private Response response;
    private String newCategoryId;

    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final int[] targetSize = {1, 10, 50, 100, 250, 500, 1000};

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
    private void trackPerformance(int size, long startSampleTime, double[] sampleTimeStore, double[] timeStore,
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

    private void printStats(String operation, double[] timeStore, double[] cpuUsageStore, long[] freeMemoryStore, double[] sampleTimeStore) {
        System.out.println("\nPerformance Stats for " + operation + " Categories");
        System.out.println("____________________________________________________________________________________________________");
        System.out.printf("| %-12s | %-18s | %-18s | %-18s | %-18s |\n", "Size", "Time (s)", "CPU Usage (%)", "Memory (MB)", "Sample Time (s)");
        System.out.println("|--------------|--------------------|--------------------|--------------------|--------------------|");

        for (int i = 0; i < targetSize.length; i++) {
            System.out.printf("| %-12d | %-18.6f | %-18.2f | %-18d | %-18.6f |\n",
                    targetSize[i],
                    timeStore[i],
                    cpuUsageStore[i],
                    freeMemoryStore[i],
                    sampleTimeStore[i]);
        }
        System.out.println("____________________________________________________________________________________________________");

        // Generate Graph for Sample Time vs Category Size
        generateGraph(operation, targetSize, sampleTimeStore);
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
                operation + " Categories - Sample Time vs Category Size",  // Chart title
                "Category Size",  // X-axis label
                "Sample Time (s)",  // Y-axis label
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
            String fileName = operation + "_Categories_SampleTime_vs_Size.png";
            File file = new File(directory, fileName);

            // Save the chart as a PNG image
            ImageIO.write(chart.createBufferedImage(800, 600), "PNG", file);
            System.out.println("Graph saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addCategoryPerformance() {
        double[] sampleTimeStore = new double[targetSize.length];
        double[] timeStore = new double[targetSize.length];
        double[] cpuUsageStore = new double[targetSize.length];
        long[] freeMemoryStore = new long[targetSize.length];
        int targetIndex = 0;
        long startSampleTime = System.nanoTime();

        for (int i = 1; i <= targetSize[targetSize.length - 1]; i++) {
            String body = "{\"title\":\"" + i + "\",\"description\":\"\"}";
            String url = "http://localhost:4567/categories";

            if (targetSize[targetIndex] == i) {
                trackPerformance(i, startSampleTime, sampleTimeStore, timeStore, cpuUsageStore, freeMemoryStore, targetIndex, body, url, "POST");
                targetIndex++;
            } else {
                executeRequest("POST", body, url);
            }
        }

        printStats("Add", timeStore, cpuUsageStore, freeMemoryStore, sampleTimeStore);
    }

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
            response = executeRequest("DELETE", body, url);
            newCategoryId = response.jsonPath().get("id");

            if (targetSize[targetIndex] == i) {
                url = "http://localhost:4567/categories/" + newCategoryId;
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
            newCategoryId = response.jsonPath().get("id");

            if (targetSize[targetIndex] == i) {
                body = "{\"title\": \"new category\"}";
                url = "http://localhost:4567/categories/" + newCategoryId;
                trackPerformance(i, startSampleTime, sampleTimeStore, timeStore, cpuUsageStore, freeMemoryStore, targetIndex, body, url, "PUT");
                targetIndex++;
            }
        }

        printStats("Change", timeStore, cpuUsageStore, freeMemoryStore, sampleTimeStore);
    }
}
