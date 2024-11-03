package steps;
import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import io.restassured.path.json.JsonPath;

import java.util.List;

public class addTodo {
    private String newTodoTitle;
    private String responseBody;
    private int responseStatus;

    static {
        baseURI = "http://localhost:4567";
    }

    @Given("I have a new to-do item with title {string}")
    public void i_have_a_new_to_do_item_with_title(String title) {
        this.newTodoTitle = title;
    }

    @When("I send a POST request to the {string} endpoint with the new to-do item")
    public void i_send_a_post_request_to_the_endpoint_with_the_new_to_do_item(String endpoint) {
        // Send POST request
        responseBody = given()
                .contentType("application/json")
                .body("{\"title\": \"" + newTodoTitle + "\"}")
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response()
                .asString();

        // capture status code
        responseStatus = given()
                .contentType("application/json")
                .body("{\"title\": \"" + newTodoTitle + "\"}")
                .when()
                .post(endpoint)
                .getStatusCode();
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(int statusCode) {
        assertEquals(statusCode, responseStatus);
    }

    @Then("the response should confirm the creation of the to-do item")
    public void the_response_should_confirm_the_creation_of_the_to_do_item() {
        // Parse the response body as JSON
        JsonPath jsonResponse = new JsonPath(responseBody);

        // assert expected fields
        assertThat(jsonResponse.getString("title"), is(newTodoTitle)); // Check title matches
        assertThat(jsonResponse.getString("doneStatus"), is("false")); // Check doneStatus is false
        assertThat(jsonResponse.getString("id"), notNullValue()); // Check id is not null
    }

    @Then("the to-do item should be saved with a unique ID")
    public void the_to_do_item_should_be_saved_with_a_unique_id() {
        // Parse the response body as JSON to get the ID of the new to-do item
        JsonPath jsonResponse = new JsonPath(responseBody);
        String todoId = jsonResponse.getString("id");

        // Fetch all to-do items
        String allTodosResponse = given()
                .when()
                .get("/todos")
                .then()
                .extract()
                .response()
                .asString();

        JsonPath allTodosJson = new JsonPath(allTodosResponse);
        List<String> allTodoIds = allTodosJson.getList("todos.id");

        // Count occurrences of the ID in the list
        long count = allTodoIds.stream().filter(id -> id.equals(todoId)).count();

        // Confirm its uniqueness
        assertEquals(1, count, "The to-do ID is not unique!");
    }

}
