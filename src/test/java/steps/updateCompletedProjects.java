package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;


public class updateCompletedProjects {
    private String responseBody;
    private int responseStatus;

    static {
        baseURI = "http://localhost:4567";
    }

    @Given("a project item exists with ID {string} and is not marked as completed")
    public void a_project_item_exists_with_ID_and_is_not_marked_as_completed(String id) {
        // Create or reset the project item with ID and ensure completed is false
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"scan paperwork\", \"completed\": false}")
                .when()
                .put("/projects/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Check that completed is set to false
        JsonPath jsonResponse = new JsonPath(responseBody);


        String completed = (String) jsonResponse.getString("completed");
        assertThat("project item should be marked as not completed.", completed, is("false"));

    }

    @When("I send a PUT request to update the status to {string} for the project item with ID {string}")
    public void i_send_a_post_request_to_update_the_status_for_the_project_item_with_ID(String status, String id) {

        // Send POST request to update the completed of the project item
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"scan paperwork\", \"completed\": " + status + "}")
                .when()
                .put("/projects/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Capture the response status code
        responseStatus = given()
                .contentType("application/json")
                .body("{\"title\": \"scan paperwork\", \"completed\": " + status + "}")
                .when()
                .put("/projects/" + id)
                .getStatusCode();
    }

    @Then("the system should mark the project item with ID {string} as completed")
    public void the_system_should_mark_the_project_item_with_ID_as_completed(String id) {
        // Check if the response status is 200
        assertEquals(200, responseStatus, "Expected status 200 for successful update.");

        // Parse the response and ensure completed is true
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat("project item should be marked as completed.", jsonResponse.getString("completed"), is("true"));
    }

    @Then("the project response should include a success message and display the updated completion status as {string}")
    public void the_response_should_include_a_success_message_and_display_the_updated_completion_status_as(String status) {
        // Check that the completed in the response matches the expected status
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat("Updated completion status should match.", jsonResponse.getString("completed"), is(status));
    }

    @Given("a project item exists with ID {string} and is marked as completed")
    public void a_project_item_exists_with_ID_and_is_marked_as_completed(String id) {
        // Create or reset the project item with ID and ensure completed is true
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"scan paperwork\", \"completed\": true}")
                .when()
                .put("/projects/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Check that completed is set to true
        JsonPath jsonResponse = new JsonPath(responseBody);
        String completed = (String) jsonResponse.getString("completed");
        assertThat("project item should be marked as completed.", completed, is("true"));
    }

    @Then("the system should mark the project item with ID {string} as not completed")
    public void the_system_should_mark_the_project_item_with_ID_as_not_completed(String id) {
        // Check if the response status is 200
        assertEquals(200, responseStatus, "Expected status 200 for successful update.");

        // Parse the response and ensure completed is false
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat("project item should be marked as not completed.", jsonResponse.getString("completed"), is("false"));
    }
    @When("I send a PUT request to update the completed field for the project item with ID {string} to null")
    public void i_send_a_post_request_to_update_the_status_for_the_project_item_without_body(String id) {

        // Send PUT request to update the completed of the project item, capture the response status code
        Response response = given()
                .contentType("application/json")
                .body("{\"completed\": null}")
                .when()
                .put("/projects/"+ id)
                .then()
                .extract()
                .response();

        responseBody = response.asString();
        responseStatus = response.getStatusCode();

    }
    @Then("the project API should return a validation error message")
    public void the_api_should_return_a_validation_error_message() {
        assertEquals(400, responseStatus); // Expecting a 400 Bad Request

        JsonPath jsonResponse = new JsonPath(responseBody);
        
        // Update the assertion to check for the actual error message returned by the API
        String actualErrorMessage = jsonResponse.getString("errorMessages");
        assertThat(actualErrorMessage, containsString("Failed Validation: completed should be BOOLEAN"));
    }
}
