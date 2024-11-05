package steps;

import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class deleteProject {
    private int responseStatus;
    private String responseBody;
    private String projectId;
    private String body;
    static {
        baseURI = "http://localhost:4567";
    }

    @Given("a project item exists for deleting")
    public void a_project_item_exists_with_ID_delete() {

        // create a project item to delete
            responseBody = given()
                    .contentType("application/json")
                    .body("{\"title\": \"Sample project\"} ")
                    .when()
                    .post("/projects")
                    .then()
                    .extract()
                    .response()
                    .asString();
            JsonPath jsonResponse = new JsonPath(responseBody);
            this.projectId = jsonResponse.get("id");

        // Verify existence of an instance by performing a GET
        responseStatus = given()
                .when()
                .get("/projects/" + projectId)
                .getStatusCode();
        assertEquals(200, responseStatus, "Expected status code 200, but got " + responseStatus);
    }

    @When("I send a DELETE request for the project item")
    public void i_send_a_DELETE_request_for_the_project_item_with_ID() {
        // Send DELETE request for the specified project ID
        responseStatus = given()
                .when()
                .delete("/projects/" + projectId)
                .getStatusCode();
    }
    @Then("the response should confirm the deletion of the project item")
    public void the_response_should_confirm_the_deletion_of_the_project_item() {
        assertEquals(200, responseStatus, "The response status code does not confirm deletion.");
    }
    @Then("the system should remove the project item")
    public void the_system_should_remove_the_project_item_with_ID() {
        // Confirm the item no longer exists by sending a GET request
        int getStatus = given()
                .when()
                .get("/projects/" + projectId)
                .getStatusCode();

        // Expect a 404 Not Found response if the item was deleted successfully
        assertEquals(404, getStatus, "The project item was not removed from the system.");
    }

    @Given("there is a project with title {string}")
    public void a_project__item_exists_with_ID_delete(String title) {

        // create a project item to delete
            responseBody = given()
                    .contentType("application/json")
                    .body("{\"title\": \"" + title + "\"}")
                    .when()
                    .post("/projects")
                    .then()
                    .extract()
                    .response()
                    .asString();
            JsonPath jsonResponse = new JsonPath(responseBody);
            this.projectId = jsonResponse.get("id");

        // Verify existence of an instance by performing a GET
        responseStatus = given()
                .when()
                .get("/projects/" + projectId)
                .getStatusCode();
        assertEquals(200, responseStatus, "Expected status code 200, but got " + responseStatus);
    }

    @Given("no project item exists with ID {string}")
    public void no_project_item_exists_with_id(String id) {
             responseStatus = given()
                .pathParam("id", id)
                .when()
                .get("/projects/{id}")
                .then()
                .extract()
                .statusCode();

        // Assert that the item does not exist
        assertEquals(404, responseStatus); // Assuming 404 is the response for not found
    }
    @When("I send a DELETE request to this project with id {string}")
    public void i_send_a_delete_request_for_the_project_item_with_id(String id) {
        // Send a DELETE request for the specified ID and capture the response status and body
        Response response = given()
                .pathParam("id", id)
                .when()
                .delete("/projects/{id}")
                .then()
                .extract()
                .response();

        responseStatus = response.getStatusCode();
        responseBody = response.asString();
    }

    @When("I send a DELETE request to this project without specifying its id")
    public void i_send_delete_project_request_without_specifying_id() {
        responseStatus = given()
                .contentType("application/json")
                .when()
                .delete("/projects/")
                .getStatusCode();
    }

    @Then("the API project response should return an error message")
    public void the_project_response_should_return_an_error_message() {
        assertEquals(404, responseStatus, "Expected 404 Not Found status code");

        // Check if the response body is not null
        assertNotNull(responseBody, "Response body is null");

        JsonPath jsonResponse = new JsonPath(responseBody);
        // Check if the response contains a general error message about missing project or invalid ID
        String errorMessage = jsonResponse.getString("errorMessages");
        assertThat(errorMessage, containsString("Could not find any instances with projects"));
    }

    @Then("the project response code returned is {int}")
    public void the_response_should_contain_a_status_code(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }
}