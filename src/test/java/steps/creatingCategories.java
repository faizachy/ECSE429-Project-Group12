package steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class creatingCategories {

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

    private String newCategoryTitle;
    private String newCategoryDescription;
    private String responseBody;
    private int responseStatus;

    static {
        baseURI = "http://localhost:4567";
    }

    @Given("I have a new category with title {string}")
    public void i_have_a_new_category_with_title(String title) {
        this.newCategoryTitle = title;
    }

    @Given("I have a new category with title {string} and description {string}")
    public void i_have_a_new_category_with_title_and_description(String title, String description) {
        this.newCategoryTitle = title;
        this.newCategoryDescription = description;
    }

    @Given("I attempt to create a new category with invalid title {string}")
    public void i_have_new_category_invalid_title(String title) {
    }


    @When("I send a POST request to the {string} endpoint with the new category using title")
    public void i_send_a_post_request_to_the_endpoint_with_the_new_category(String endpoint) {
        // Send POST request
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"" + newCategoryTitle + "\"}")
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response()
                .asString();

        // capture status code
        responseStatus = given()
                .contentType("application/json")
                .body("{\"title\": \"" + newCategoryTitle + "\"}")
                .when()
                .post(endpoint)
                .getStatusCode();
    }

    @When("I send a POST request to the {string} endpoint with the new category using title and description")
    public void i_send_a_post_request_to_the_endpoint_with_the_new_category_desc(String endpoint) {
        // Send POST request
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"" + newCategoryTitle + "\", \"description\": \"" + newCategoryDescription + "\"}")
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response()
                .asString();

        // capture status code
        responseStatus = given()
                .contentType("application/json")
                .body("{\"title\": \"" + newCategoryTitle + "\", \"description\": \"" + newCategoryDescription + "\"}")
                .when()
                .post(endpoint)
                .getStatusCode();
    }

    @When("I send a POST request to the {string} endpoint with the new category with invalid title")
    public void i_send_a_post_request_to_the_endpoint_with_the_new_category_invalid_title(String endpoint) {
        // Send POST request
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"" + "\"}")
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response()
                .asString();

        // capture status code
        responseStatus = given()
                .contentType("application/json")
                .body("{\"title\": \"" + "\"}")
                .when()
                .post(endpoint)
                .getStatusCode();
    }


    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @Then("the response should confirm the creation of the category")
    public void the_response_should_confirm_the_creation_of_the_category() {
        // Parse the response body as JSON
        JsonPath jsonResponse = new JsonPath(responseBody);

        // assert expected fields
        assertThat(jsonResponse.getString("title"), is(newCategoryTitle)); // Check title matches
        assertThat(jsonResponse.getString("id"), notNullValue()); // Check id is not null
    }

    @Then("the response should confirm the creation of new category with title and description")
    public void the_response_should_confirm_the_creation_of_the_category_with_title_and_desc() {
        // Parse the response body as JSON
        JsonPath jsonResponse = new JsonPath(responseBody);

        // assert expected fields
        assertThat(jsonResponse.getString("title"), is(newCategoryTitle)); // Check title matches
        assertThat(jsonResponse.getString("description"), is(newCategoryDescription)); // Check title matches
        assertThat(jsonResponse.getString("id"), notNullValue()); // Check id is not null
    }
}
