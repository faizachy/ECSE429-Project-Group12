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

public class getCategories {

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

    private String responseBody;
    private int responseStatus;

    static {
        baseURI = "http://localhost:4567";
    }

    @Given("a category exists with title {string}")
    public void a_category_exists_with_title(String title) {
        given()
                .contentType("application/json")
                .body("{\"title\": \"" + title + "\", \"description\": \"\"}")
                .when()
                .post("/categories");
    }

    @When("I send a GET request for all categories")
    public void i_send_get_request_for_all_categories() {
        responseBody = given()
                .contentType("application/json")
                .when()
                .get("/categories")
                .then()
                .extract()
                .response()
                .asString();

        responseStatus = given()
                .contentType("application/json")
                .when()
                .get("/categories")
                .getStatusCode();
    }

    @When("I send a GET request for category with id {string}")
    public void i_send_get_request_for_category_with_id(String id) {
        responseBody = given()
                .contentType("application/json")
                .when()
                .get("/categories/" + id)
                .then()
                .extract()
                .response()
                .asString();

        responseStatus = given()
                .contentType("application/json")
                .when()
                .get("/categories/" + id)
                .getStatusCode();
    }

    @Then("the response code should be {int}")
    public void the_response_code_should_be_int(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @Then("the response body should contain a list of categories")
    public void the_response_body_should_contain_a_list_of_categories() {
        assertThat(responseBody, containsString("["));
    }
}
