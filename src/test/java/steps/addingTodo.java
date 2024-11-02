package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class addingTodo {
        private String newTodoTitle;
        private String responseBody;
        private int responseStatus;

        @Given("I have a new to-do item with title {string}")
        public void i_have_a_new_to_do_item_with_title(String title) {
            this.newTodoTitle = title;
        }

        @When("I send a POST request to the {string} endpoint with the new to-do item")
        public void i_send_a_post_request_to_the_endpoint_with_the_new_to_do_item(String endpoint) {
            responseBody = given()
                    .contentType("application/json")
                    .body("{\"title\": \"" + newTodoTitle + "\"}")
                    .when()
                    .post(endpoint)
                    .then()
                    .extract()
                    .response()
                    .asString();

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
            assertThat(responseBody, containsString("success"));
        }

        @Then("the to-do item should be saved with a unique ID")
        public void the_to_do_item_should_be_saved_with_a_unique_id() {
            // Here you could also implement a way to verify the item is in the database,
            // by querying the API to check if the item exists with the expected title.
        }

}
