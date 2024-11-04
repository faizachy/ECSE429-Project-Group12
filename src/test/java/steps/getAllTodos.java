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

public class getAllTodos {
    private String responseBody;
    private int responseStatus;
    private List<Object> todoList;
    static {
        baseURI = "http://localhost:4567";
    }

    @Given("there are multiple to-do items in the system")
    public void there_are_multiple_to_do_items_in_the_system() {
        responseBody = given()
                .when()
                .get("/todos")
                .then()
                .extract()
                .response()
                .asString();

        JsonPath jsonResponse = new JsonPath(responseBody);
        todoList = jsonResponse.getList("todos");

        // Check if there are already multiple items; if not, add some
        if (todoList.size() < 2) {
            given().contentType("application/json")
                    .body("{\"title\": \"To-Do Item 1\"}")
                    .when().post("/todos");

            given().contentType("application/json")
                    .body("{\"title\": \"To-Do Item 2\"}")
                    .when().post("/todos");
        }
    }

    @When("I send a GET request to retrieve all to-do items")
    public void i_send_a_get_request_to_retrieve_all_to_do_items() {
        // Send GET request to retrieve all to-do items and capture response body
        responseBody = given()
                .when()
                .get("/todos")
                .then()
                .extract()
                .response()
                .asString();

        // Get the response status code
        responseStatus = given().when().get("/todos").getStatusCode();
    }

    @Then("the API should return a list of all items with their details")
    public void the_api_should_return_a_list_of_all_items_with_their_details() {
        // Check that the response status code is 200
        assertEquals(200, responseStatus, "Expected status code 200 for successful retrieval.");

        // Parse response body as JSON
        JsonPath jsonResponse = new JsonPath(responseBody);

        // Check that the list of items is not empty
        todoList = jsonResponse.getList("todos");
        assertThat("The list of to-do items should not be empty.", todoList.size(), greaterThan(0));
    }

    @Then("each to-do item in the list should display its ID, title, and completion status")
    public void each_to_do_item_in_the_list_should_display_its_ID_title_and_completion_status() {

        // Verify that each item has the required fields
        for (Object todoObject : todoList) {
            Map<String, Object> todo = (Map<String, Object>) todoObject;

            String id = (String) todo.get("id");
            String title = (String) todo.get("title");
            String doneStatus = (String) todo.get("doneStatus");
            String description = (String) todo.get("description");

            // Check that the values are not null
            assertThat("ID should not be null", id, notNullValue());
            assertThat("Title should not be null", title, notNullValue());
            assertThat("Done status should not be null", doneStatus, notNullValue());
            assertThat("Description should not be null", description, notNullValue());

            System.out.println("Todo ID: " + id + ", Title: " + title + ", Done Status: " + doneStatus + ", Description: " + description);
        }
    }
    @Given("there is a to-do item with ID {string} in the system")
    public void there_is_a_to_do_item_with_id_in_the_system(String id) {
        // Capture the response status
        responseStatus = given().when().get("/todos/" + id).getStatusCode();
        // Validate the response status
        if (responseStatus != 200) {
            throw new IllegalStateException("To-Do item with ID " + id + " does not exist. Status code: " + responseStatus);
        }
    }

    @When("I send a GET request to retrieve the to-do object with ID {string}")
    public void i_send_a_get_request_to_retrieve_the_to_do_items_with_id(String id) {
        Response response = given()
                .when()
                .get("/todos/" + id);

        // Extract response body and status code
        responseBody = response.asString();
        responseStatus = response.getStatusCode();
    }

    @Then("the API should only return the details of the to-do item with ID {string}")
    public void the_api_should_only_return_the_details_of_the_to_do_item_with_id(String id) {
        // Check that the response status is 200 for successful retrieval
        assertEquals(200, responseStatus, "Expected status 200 for existing to-do item retrieval.");
        JsonPath jsonResponse = new JsonPath(responseBody);
        List<Object> todo = jsonResponse.getList("todos");
        // Check that the size of the list is exactly 1
        assertEquals(1, todo.size(), "Expected exactly one to-do item, but found " +  todo.size());
    }
}
