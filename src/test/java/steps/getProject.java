package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.path.json.JsonPath;


import java.util.List;
import java.util.Map;

public class getProject {
    private String responseBody;
    private int responseStatus;
    private List<Object> todo;
    private Map<String, Object> todoObject;
    private String id;

    static {
        baseURI = "http://localhost:4567";
    }

    @Given("a project item exists for ID {string}")
    public void a_project_item_exists_with_ID_get(String expectedId) {
        // Verify existence of an instance by performing a GET
        responseStatus = given()
                .when()
                .get("/projects/" + expectedId)
                .getStatusCode();

        // If it doesn't exist, create it for testing
        if (responseStatus == 404) {
            // Create the project item without specifying an ID
            responseBody = given()
                    .contentType("application/json")
                    .body("{\"title\": \"Sample project\"}") // Don't specify ID here
                    .when()
                    .post("/projects")
                    .then()
                    .extract()
                    .response()
                    .asString();

            // Validate the response status after creation
            int creationStatus = given().when().get("/projects").getStatusCode();
            assertEquals(200, creationStatus, "Expected status 200 for fetching all project items.");

            // Parse the response body to find the newly created item
            JsonPath jsonResponse = new JsonPath(responseBody);
            String createdId = jsonResponse.getString("id");

            System.out.println("Created project item ID: " + createdId);
            assertNotNull(createdId, "The created project item ID should not be null.");
        }
    }


    @When("I send a GET request to retrieve the project item with ID {string}")
    public void i_send_a_get_request_to_retrieve_the_project_item_with_ID(String id) {
        // Send a GET request for the specific project item by ID
        responseBody = given()
                .when()
                .get("/projects/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Capture the response status
        responseStatus = given().when().get("/projects/" + id).getStatusCode();
    }

    @Then("the API should return the details of only one project item")
    public void the_api_should_return_the_details_of_only_one_project_item() {
        // Check that the response status is 200 for successful retrieval
        assertEquals(200, responseStatus, "Expected status 200 for existing project item retrieval.");
        JsonPath jsonResponse = new JsonPath(responseBody);
        todo = jsonResponse.getList("projects");
        // Check that the size of the list is exactly 1
        assertEquals(1, todo.size(), "Expected exactly one project item, but found " +  todo.size());

    }

    @Then("the ID for the project item should be {string}")
    public void the_id_should_be_as_expected(String expectedID) {
        if (!todo.isEmpty()) {
            todoObject = (Map<String, Object>) todo.get(0);
            // Extract id from the project item
            id = (String) todoObject.get("id");
            assertEquals(expectedID, id, "The ID for the project item does not match the expected ID.");
        } else {
            fail("The project list is empty. Expected at least one project item.");
        }

    }
    @Then("the project response should include the following details:")
    public void the_response_should_include_the_following_details(io.cucumber.datatable.DataTable dataTable) {
        if (!todo.isEmpty()) {
            // expected details
            Map<String, Object> todoObject = (Map<String, Object>) todo.get(0);

            // Convert the DataTable to a list of maps for easier access
            List<Map<String, String>> expectedDetails = dataTable.asMaps(String.class, String.class);

            // Iterate through each expected detail and verify
            for (Map<String, String> detail : expectedDetails) {
                String field = detail.get("field");
                String expectedValue = detail.get("value");
                String actualValue = (String) todoObject.get(field);

                // Assert that the actual value matches the expected value
                assertEquals(expectedValue, actualValue, "The " + field + " does not match the expected value. Expected: " + expectedValue + ", but got: " + actualValue);
            }
        } else {
            fail("The project list is empty. Expected at least one project item.");
        }
    }

    @Given("there is no project item with ID {string}")
    public void there_is_no_project_item_with_ID(String id) {
        // Attempt to delete any existing item with the given ID to ensure it doesn't exist
        given()
                .when()
                .delete("/projects/" + id)
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }
    @When("I send a GET request to retrieve project item with ID {string}")
    public void i_send_a_get_request_to_retrieve_project_item_with_ID(String id) {
        // Send GET request to retrieve the project item
        responseBody = given()
                .when()
                .get("/projects/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Capture the response status code
        responseStatus = given()
                .when()
                .get("/projects/" + id)
                .then()
                .extract()
                .response()
                .getStatusCode();
    }
    @Then("the API should return an error message indicating that the project item does not exist")
    public void the_api_should_return_an_error_message_indicating_that_the_project_item_does_not_exist() {
        // Check that the response status code is 404, indicating the item was not found
        assertEquals(404, responseStatus, "Expected status 404 for non-existent project item retrieval.");

        // Parse the response body to confirm the presence of an error message
        JsonPath jsonResponse = new JsonPath(responseBody);
        String errorMessage = jsonResponse.getString("errorMessages");
        assertThat("An error message should be present.", errorMessage, notNullValue());
    }

    @When("I send a malformed GET request for project with an invalid ID format")
    public void get_request_sent_with_malformed_request(){
        responseStatus = given()
                .when()
                .get("/projects/" + "string ID")
                .getStatusCode();
        responseBody = given()
                .pathParam("id", "string ID")
                .when()
                .get("/projects/{id}")
                .then()
                .extract()
                .body()
                .asString();


    }
    @Then("the project API should return an error")
    public void the_response_should_return_an_error() {
        assertEquals(404, responseStatus); // Expecting a 404
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat(jsonResponse.getString("errorMessages"),
                is("[Could not find an instance with projects/string ID]"));
    }
}
