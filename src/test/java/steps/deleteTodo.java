package steps;

import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class deleteTodo {
    private int responseStatus;
    private String responseBody;
    private String todoId;
    private String body;
    static {
        baseURI = "http://localhost:4567";
    }

    @Given("a to-do item exists for deleting")
    public void a_to_do_item_exists_with_ID_delete() {

        // create a todo item to delete
            responseBody = given()
                    .contentType("application/json")
                    .body("{\"title\": \"Sample To-Do\"} ")
                    .when()
                    .post("/todos")
                    .then()
                    .extract()
                    .response()
                    .asString();
            JsonPath jsonResponse = new JsonPath(responseBody);
            this.todoId = jsonResponse.get("id");

        // Verify existence of an instance by performing a GET
        responseStatus = given()
                .when()
                .get("/todos/" + todoId)
                .getStatusCode();
        assertEquals(200, responseStatus, "Expected status code 200, but got " + responseStatus);
    }

    @When("I send a DELETE request for the to-do item")
    public void i_send_a_DELETE_request_for_the_to_do_item_with_ID() {
        // Send DELETE request for the specified to-do ID
        responseStatus = given()
                .when()
                .delete("/todos/" + todoId)
                .getStatusCode();
    }
    @Then("the response should confirm the deletion of the to-do item")
    public void the_response_should_confirm_the_deletion_of_the_to_do_item() {
        assertEquals(200, responseStatus, "The response status code does not confirm deletion.");
    }
    @Then("the system should remove the to-do item")
    public void the_system_should_remove_the_to_do_item_with_ID() {
        // Confirm the item no longer exists by sending a GET request
        int getStatus = given()
                .when()
                .get("/todos/" + todoId)
                .getStatusCode();

        // Expect a 404 Not Found response if the item was deleted successfully
        assertEquals(404, getStatus, "The to-do item was not removed from the system.");
    }
    @Given("no to-do item exists with ID {string}")
    public void no_todo_item_exists_with_id(String id) {
             responseStatus = given()
                .pathParam("id", id)
                .when()
                .get("/todos/{id}")
                .then()
                .extract()
                .statusCode();

        // Assert that the item does not exist
        assertEquals(404, responseStatus); // Assuming 404 is the response for not found
    }
    @When("I send a DELETE request for the to-do item with ID {string}")
    public void i_send_a_delete_request_for_the_todo_item_with_id(String id) {
        // Send a DELETE request for the specified ID and capture the response status
        responseStatus = given()
                .pathParam("id", id)
                .when()
                .delete("/todos/{id}")
                .then()
                .extract()
                .statusCode();
        responseBody = given()
                .pathParam("id", id)
                .when()
                .delete("/todos/{id}")
                .then()
                .extract()
                .body()
                .asString();
    }
    @Then("the API should return an error indicating that the item could not be found")
    public void the_api_should_return_an_error_indicating_that_the_item_could_not_be_found() {
        assertEquals(404, responseStatus); // Assuming 404 for not found
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat(jsonResponse.getString("errorMessages"),
                is("[Could not find any instances with todos/123]"));
    }

    @When("I send the malformed DELETE request with an invalid ID format")
    public void delete_request_sent_with_malformed_request(){
        responseStatus = given()
                .when()
                .delete("/todos/" + "string ID")
                .getStatusCode();
        responseBody = given()
                .pathParam("id", "string ID")
                .when()
                .delete("/todos/{id}")
                .then()
                .extract()
                .body()
                .asString();
    }
    @Then("the API response should return an error message")
    public void the_response_should_return_an_error_message() {
        assertEquals(404, responseStatus); // Expecting a 404
        JsonPath jsonResponse = new JsonPath(responseBody);
        assertThat(jsonResponse.getString("errorMessages"),
                is("[Could not find any instances with todos/string ID]"));
    }
}
