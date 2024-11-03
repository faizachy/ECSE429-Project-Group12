package steps;

import io.cucumber.java.en.*;
import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

public class deleteTodo {
    private int responseStatus;
    private String responseBody;
    private String todoId;

    static {
        baseURI = "http://localhost:4567"; // Adjust as needed
    }

    @Given("a to-do item exists for deleting with ID {string}")
    public void a_to_do_item_exists_with_ID_delete(String id) {
        this.todoId = id;

        // Verify existence of an instance by performing a GET
        responseStatus = given()
                .when()
                .get("/todos/" + todoId)
                .getStatusCode();

        // If it doesn't exist, create it for testing
        if (responseStatus == 404) {
            responseBody = given()
                    .contentType("application/json")
                    .body("{\"title\": \"Sample To-Do\", \"id\": \"" + todoId + "\"}")
                    .when()
                    .post("/todos")
                    .then()
                    .extract()
                    .response()
                    .asString();
        }
    }

    @When("I send a DELETE request for the to-do item with ID {string}")
    public void i_send_a_DELETE_request_for_the_to_do_item_with_ID(String id) {
        // Send DELETE request for the specified to-do ID
        responseStatus = given()
                .when()
                .delete("/todos/" + id)
                .getStatusCode();
    }
    @Then("the response should confirm the deletion of the to-do item")
    public void the_response_should_confirm_the_deletion_of_the_to_do_item() {
        assertEquals(200, responseStatus, "The response status code does not confirm deletion.");
    }
    @Then("the system should remove the to-do item with ID {string}")
    public void the_system_should_remove_the_to_do_item_with_ID(String id) {
        // Confirm the item no longer exists by sending a GET request
        int getStatus = given()
                .when()
                .get("/todos/" + id)
                .getStatusCode();

        // Expect a 404 Not Found response if the item was deleted successfully
        assertEquals(404, getStatus, "The to-do item was not removed from the system.");
    }


}
