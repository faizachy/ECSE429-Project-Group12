package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

public class updateDoneStatusTodo {
    private String responseBody;
    private int responseStatus;
    private String todoId = "123"; // ID for the to-do item

    static {
        baseURI = "http://localhost:4567"; // Adjust as needed
    }

    @Given("a to-do item exists with ID {string} and is not marked as completed")
    public void a_to_do_item_exists_with_ID_and_is_not_marked_as_completed(String id) {
        // Create or reset the to-do item with ID and ensure doneStatus is false
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"scan paperwork\", \"doneStatus\": false}")
                .when()
                .put("/todos/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Check that doneStatus is set to false
        JsonPath jsonResponse = new JsonPath(responseBody);


        String doneStatus = (String) jsonResponse.getString("doneStatus");
        assertThat("To-do item should be marked as not completed.", doneStatus, is("false"));

    }

    @When("I send a PUT request to update the status to {string} for the to-do item with ID {string}")
    public void i_send_a_post_request_to_update_the_status_for_the_to_do_item_with_ID(String status, String id) {

        // Send POST request to update the doneStatus of the to-do item
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"scan paperwork\", \"doneStatus\": " + status + "}")
                .when()
                .put("/todos/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Capture the response status code
        responseStatus = given()
                .contentType("application/json")
                .body("{\"title\": \"scan paperwork\", \"doneStatus\": " + status + "}")
                .when()
                .put("/todos/" + id)
                .getStatusCode();
    }

    @Then("the system should mark the to-do item with ID {string} as completed")
    public void the_system_should_mark_the_to_do_item_with_ID_as_completed(String id) {
        // Check if the response status is 200
        assertEquals(200, responseStatus, "Expected status 200 for successful update.");

        // Parse the response and ensure doneStatus is true
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat("To-do item should be marked as completed.", jsonResponse.getString("doneStatus"), is("true"));
    }

    @Then("the response should include a success message and display the updated completion status as {string}")
    public void the_response_should_include_a_success_message_and_display_the_updated_completion_status_as(String status) {
        // Check that the doneStatus in the response matches the expected status
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat("Updated completion status should match.", jsonResponse.getString("doneStatus"), is(status));
    }

    @Given("a to-do item exists with ID {string} and is marked as completed")
    public void a_to_do_item_exists_with_ID_and_is_marked_as_completed(String id) {
        // Create or reset the to-do item with ID and ensure doneStatus is true
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"scan paperwork\", \"doneStatus\": true}")
                .when()
                .put("/todos/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Check that doneStatus is set to true
        JsonPath jsonResponse = new JsonPath(responseBody);
        String doneStatus = (String) jsonResponse.getString("doneStatus");
        assertThat("To-do item should be marked as completed.", doneStatus, is("true"));
    }

    @Then("the system should mark the to-do item with ID {string} as not completed")
    public void the_system_should_mark_the_to_do_item_with_ID_as_not_completed(String id) {
        // Check if the response status is 200
        assertEquals(200, responseStatus, "Expected status 200 for successful update.");

        // Parse the response and ensure doneStatus is false
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat("To-do item should be marked as not completed.", jsonResponse.getString("doneStatus"), is("false"));
    }
    @When("I send a PUT request to update the status for the to-do item with ID {string} with empty body")
    public void i_send_a_post_request_to_update_the_status_for_the_to_do_item_without_body(String id) {

        // Send PUT request to update the doneStatus of the to-do item, capture the response status code
        Response response = given()
                .contentType("application/json")
                .when()
                .put("/todos/"+ id)
                .then()
                .extract()
                .response();

        responseBody = response.asString();
        responseStatus = response.getStatusCode();

    }
    @Then("the API should return a validation error message")
    public void the_api_should_return_a_validation_error_message() {
        assertEquals(400, responseStatus); // Expecting a 400 Bad Request
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat(jsonResponse.getString("errorMessages"),
                is("[title : field is mandatory]"));
    }
}
