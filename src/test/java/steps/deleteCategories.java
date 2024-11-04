package steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class deleteCategories {
    private HttpClient client;
    public static String categoryId = "0";


    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        categoryId = createCategoryAndGetId();

    }

    @AfterEach
    public void restore() throws IOException, InterruptedException {
        deleteCategoryById(categoryId);
    }

    private static void deleteCategoryById(String categoryId) throws IOException, InterruptedException {
        String id = createCategoryAndGetId();
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/categories/" + id))
                .DELETE()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode(), "Failed to delete project with id: " + categoryId);
    }

    private static String createCategoryAndGetId() throws IOException, InterruptedException {
        String categoryRequestBody = "{ \"title\": \"Category Title\", \"description\": \"Category Description\" }";
        HttpRequest categoryRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/categories"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(categoryRequestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> categoryResponse = client.send(categoryRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, categoryResponse.statusCode());

        String responseBody = categoryResponse.body();
        String id = new ObjectMapper().readTree(responseBody).get("id").asText();

        return id;
    }

    private int responseStatus;


    static {
        baseURI = "http://localhost:4567";
    }

    @Given("there is a category with title {string}")
    public void there_is_category_with_title(String title) {
        String responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"" + title + "\", \"description\": \"\"}")
                .when()
                .post("/categories")
                .then()
                .extract()
                .response()
                .asString();

        categoryId = responseBody.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    @When("I send a DELETE request to this category")
    public void i_send_delete_request_to_category() {
        responseStatus = given()
                .contentType("application/json")
                .when()
                .delete("/categories/" + categoryId)
                .getStatusCode();
    }

    @When("I send a DELETE request to this category without specifying its id")
    public void i_send_delete_request_without_specifying_id() {
        responseStatus = given()
                .contentType("application/json")
                .when()
                .delete("/categories/")
                .getStatusCode();
    }

    @When("I send a DELETE request to this category with id {string}")
    public void i_send_delete_request_to_category_with_id(String id) {
        responseStatus = given()
                .contentType("application/json")
                .when()
                .delete("/categories/" + id)
                .getStatusCode();
    }

    @Then("the response code returned is {int}")
    public void the_response_should_contain_a_status_code(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }
}
