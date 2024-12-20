package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.path.json.JsonPath;


import java.util.List;
import java.util.Map;

public class getTodo {
    private String responseBody;
    private int responseStatus;
    private List<Object> todo;
    private Map<String, Object> todoObject;
    private String id;

    static {
        baseURI = "http://localhost:4567";
    }

    @Given("a to-do item exists for getting with ID {string}")
    public void a_to_do_item_exists_with_ID_get(String expectedId) {
        // Verify existence of an instance by performing a GET
        responseStatus = given()
                .when()
                .get("/todos/" + expectedId)
                .getStatusCode();

        // If it doesn't exist, create it for testing
        if (responseStatus == 404) {
            // Create the to-do item without specifying an ID
            responseBody = given()
                    .contentType("application/json")
                    .body("{\"title\": \"Sample To-Do\"}") // Don't specify ID here
                    .when()
                    .post("/todos")
                    .then()
                    .extract()
                    .response()
                    .asString();

            // Validate the response status after creation
            int creationStatus = given().when().get("/todos").getStatusCode();
            assertEquals(200, creationStatus, "Expected status 200 for fetching all to-do items.");

            // Parse the response body to find the newly created item
            JsonPath jsonResponse = new JsonPath(responseBody);
            String createdId = jsonResponse.getString("id");

            System.out.println("Created to-do item ID: " + createdId);
            assertNotNull(createdId, "The created to-do item ID should not be null.");
        }
    }


    @When("I send a GET request to retrieve the to-do item with ID {string}")
    public void i_send_a_get_request_to_retrieve_the_to_do_item_with_ID(String id) {
        // Send a GET request for the specific to-do item by ID
        responseBody = given()
                .when()
                .get("/todos/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Capture the response status
        responseStatus = given().when().get("/todos/" + id).getStatusCode();
    }

    @Then("the API should return the details of only one to-do item")
    public void the_api_should_return_the_details_of_only_one_to_do_item() {
        // Check that the response status is 200 for successful retrieval
        assertEquals(200, responseStatus, "Expected status 200 for existing to-do item retrieval.");
        JsonPath jsonResponse = new JsonPath(responseBody);
        todo = jsonResponse.getList("todos");
        // Check that the size of the list is exactly 1
        assertEquals(1, todo.size(), "Expected exactly one to-do item, but found " +  todo.size());

    }

    @Then("the ID for the item should be {string}")
    public void the_id_should_be_as_expected(String expectedID) {
        if (!todo.isEmpty()) {
            todoObject = (Map<String, Object>) todo.get(0);
            // Extract id from the to-do item
            id = (String) todoObject.get("id");
            assertEquals(expectedID, id, "The ID for the to-do item does not match the expected ID.");
        } else {
            fail("The to-do list is empty. Expected at least one to-do item.");
        }

    }
    @Then("the response should include the following details:")
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
            fail("The to-do list is empty. Expected at least one to-do item.");
        }
    }

    @Given("there is no to-do item with ID {string}")
    public void there_is_no_to_do_item_with_ID(String id) {
        // Attempt to delete any existing item with the given ID to ensure it doesn't exist
        given()
                .when()
                .delete("/todos/" + id)
                .then()
                .statusCode(anyOf(is(200), is(404)));
    }
    @When("I send a GET request to retrieve to-do item with ID {string}")
    public void i_send_a_get_request_to_retrieve_to_do_item_with_ID(String id) {
        // Send GET request to retrieve the to-do item
        responseBody = given()
                .when()
                .get("/todos/" + id)
                .then()
                .extract()
                .response()
                .asString();

        // Capture the response status code
        responseStatus = given()
                .when()
                .get("/todos/" + id)
                .then()
                .extract()
                .response()
                .getStatusCode();
    }
    @Then("the API should return an error message indicating that the to-do item does not exist")
    public void the_api_should_return_an_error_message_indicating_that_the_to_do_item_does_not_exist() {
        // Check that the response status code is 404, indicating the item was not found
        assertEquals(404, responseStatus, "Expected status 404 for non-existent to-do item retrieval.");

        // Parse the response body to confirm the presence of an error message
        JsonPath jsonResponse = new JsonPath(responseBody);
        String errorMessage = jsonResponse.getString("errorMessages");
        assertThat("An error message should be present.", errorMessage, notNullValue());
    }

    @When("I send a malformed GET request with an invalid ID format")
    public void get_request_sent_with_malformed_request(){
        responseStatus = given()
                .when()
                .get("/todos/" + "string ID")
                .getStatusCode();
        responseBody = given()
                .pathParam("id", "string ID")
                .when()
                .get("/todos/{id}")
                .then()
                .extract()
                .body()
                .asString();


    }
    @Then("the API should return an error")
    public void the_response_should_return_an_error() {
        assertEquals(404, responseStatus); // Expecting a 404
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat(jsonResponse.getString("errorMessages"),
                is("[Could not find an instance with todos/string ID]"));
    }
}
