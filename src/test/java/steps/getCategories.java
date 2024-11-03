package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class getCategories {
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
