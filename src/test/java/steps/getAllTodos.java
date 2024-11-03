package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.path.json.JsonPath;
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
        given().contentType("application/json")
                .body("{\"title\": \"To-Do Item 1\"}")
                .when().post("/todos");

        given().contentType("application/json")
                .body("{\"title\": \"To-Do Item 2\"}")
                .when().post("/todos");

        // verify items exist by performing a GET request and checking the response
        responseStatus = given().when().get("/todos").getStatusCode();
        assertEquals(200, responseStatus, "Expected status 200, but got " + responseStatus);
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
}
