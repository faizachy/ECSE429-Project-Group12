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

public class viewCategories {

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

    @Given("that a category having ID {int} exists")
    public void category_having_id_exists(int id) {
        String body = "{\"title\": \"Office\", \"description\": \"\"}";
        if (id == 4) {
            body = "{\"title\": \"Category 4 Title\", \"description\": \"Description Test\"}";
        }

        given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/categories");
    }

    @When("I send a request to view the details of the category having ID {int}")
    public void i_send_request_to_view_details_of_category_using_id(int id) {
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

    @Then("the title of category with ID {int} retrieved is {string}")
    public void the_title_of_category_with_id_retrieved_is(int id, String title) {
        assertThat(responseBody, containsString("\"title\":\"" + title + "\"")); // Check title matches
    }

    @Then("the description of category with ID {int} retrieved is {string}")
    public void the_description_category_with_id_retrieved_is(int id, String description) {
        assertThat(responseBody, containsString("\"description\":\"" + description + "\"")); // Check description matches
    }

    @Given("that a category having ID {int} does not exist")
    public void category_having_id_does_not_exist(int id) {
        // No setup needed
    }

    @Then("I should receive an error message indicating that the category with ID {int} wasn't found")
    public void i_should_receive_an_error_message_indicating_that_category_with_id_was_not_found(int id) {
        responseStatus = given()
                .contentType("application/json")
                .when()
                .get("/categories/" + id)
                .getStatusCode();

        assertEquals(404, responseStatus);
    }
}

