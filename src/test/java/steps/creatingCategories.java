package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import io.restassured.path.json.JsonPath;

public class creatingCategories {
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
    public void i_have_new_category_invalid_title(String title){
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
                .body("{\"title\": \""  + "\"}")
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response()
                .asString();

        // capture status code
        responseStatus = given()
                .contentType("application/json")
                .body("{\"title\": \""  + "\"}")
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
