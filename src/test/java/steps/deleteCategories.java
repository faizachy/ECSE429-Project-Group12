package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class deleteCategories {
    private int responseStatus;
    private String categoryId;

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
