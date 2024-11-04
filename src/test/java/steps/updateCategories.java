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
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class updateCategories {

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

    @Given("a category that has title {string} exists")
    public void a_category_that_has_title_exists(String title) {
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

    @When("I send a request to update the title of category to {string}")
    public void i_send_request_to_update_title_of_category(String newTitle) {
        String requestBody = "{\"title\": \"" + newTitle + "\"}";

        responseStatus = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/categories/" + categoryId)
                .getStatusCode();
    }

    @When("I send a request to update the title of category to {string} and description {string}")
    public void i_send_request_to_update_title_and_description_of_category(String newTitle, String newDescription) {
        String requestBody = "{\"title\": \"" + newTitle + "\", \"description\": \"" + newDescription + "\"}";

        responseStatus = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/categories/" + categoryId)
                .getStatusCode();
    }

    @When("I send a request to update the title of category with id {string} to {string}")
    public void i_send_request_to_update_title_of_category_using_id(String id, String newTitle) {
        String requestBody = "{\"title\": \"" + newTitle + "\"}";

        responseStatus = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/categories/" + id)
                .getStatusCode();
    }

    @Then("the response code that is returned is {int}")
    public void the_response_code_that_is_returned(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @Then("the updated title will be {string}")
    public void the_update_title_will_be(String title) {
        String responseBody = given()
                .contentType("application/json")
                .when()
                .get("/categories/" + categoryId)
                .then()
                .extract()
                .response()
                .asString();

        assertThat(responseBody, containsString("\"title\":\"" + title + "\"")); // Check title is updated
    }

    @Then("the updated title will be {string} and description {string}")
    public void the_updated_title_and_description(String title, String description) {
        String responseBody = given()
                .contentType("application/json")
                .when()
                .get("/categories/" + categoryId)
                .then()
                .extract()
                .response()
                .asString();

        assertThat(responseBody, containsString("\"title\":\"" + title + "\"")); // Check title is updated
        assertThat(responseBody, containsString("\"description\":\"" + description + "\"")); // Check description is updated
    }
}
