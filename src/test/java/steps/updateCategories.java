package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class updateCategories {
    private int responseStatus;
    private String categoryId;

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
