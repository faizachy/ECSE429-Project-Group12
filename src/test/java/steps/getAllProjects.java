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

public class getAllProjects {
    private String responseBody;
    private int responseStatus;
    private List<Object> projectList;
    static {
        baseURI = "http://localhost:4567";
    }

    @Given("there are multiple project items in the system")
    public void there_are_multiple_project_items_in_the_system() {
        responseBody = given()
                .when()
                .get("/projects")
                .then()
                .extract()
                .response()
                .asString();

        JsonPath jsonResponse = new JsonPath(responseBody);
        projectList = jsonResponse.getList("projects");

        // Check if there are already multiple items; if not, add some
        if (projectList.size() < 2) {
            given().contentType("application/json")
                    .body("{\"title\": \"project Item 1\"}")
                    .when().post("/projects");

            given().contentType("application/json")
                    .body("{\"title\": \"project Item 2\"}")
                    .when().post("/projects");
        }
    }

    @When("I send a GET request to retrieve all project items")
    public void i_send_a_get_request_to_retrieve_all_project_items() {
        // Send GET request to retrieve all project items and capture response body
        responseBody = given()
                .when()
                .get("/projects")
                .then()
                .extract()
                .response()
                .asString();

        // Get the response status code
        responseStatus = given().when().get("/projects").getStatusCode();
    }

    @Then("the API should return a list of all project items with their details")
    public void the_api_should_return_a_list_of_all_project_items_with_their_details() {
        // Check that the response status code is 200
        assertEquals(200, responseStatus, "Expected status code 200 for successful retrieval.");

        // Parse response body as JSON
        JsonPath jsonResponse = new JsonPath(responseBody);

        // Check that the list of items is not empty
        projectList = jsonResponse.getList("projects");
        assertThat("The list of project items should not be empty.", projectList.size(), greaterThan(0));
    }

    @Then("each project item in the list should display its ID, title, description, completed and active")
    public void each_project_item_in_the_list_should_display_its_ID_title_description_active_and_completion_status() {

        // Verify that each item has the required fields
        for (Object projectObject : projectList) {
            Map<String, Object> project = (Map<String, Object>) projectObject;

            String id = (String) project.get("id");
            String title = (String) project.get("title");
            String completed = (String) project.get("completed");
            String active = (String) project.get("active");
            String description = (String) project.get("description");

            // Check that the values are not null
            assertThat("ID should not be null", id, notNullValue());
            assertThat("Title should not be null", title, notNullValue());
            assertThat("Completed should not be null", completed, notNullValue());
            assertThat("Active should not be null", active, notNullValue());
            assertThat("Description should not be null", description, notNullValue());

            System.out.println("project ID: " + id + ", Title: " + title + ", Completed: " + completed + ", Active: " + active + " , Description: " + description);
        }
    }
    @Given("there is a project item with ID {string} in the system")
    public void there_is_a_project_item_with_id_in_the_system(String id) {
        // Capture the response status
        responseStatus = given().when().get("/projects/" + id).getStatusCode();
        // Validate the response status
        if (responseStatus != 200) {
            throw new IllegalStateException("project item with ID " + id + " does not exist. Status code: " + responseStatus);
        }
    }

    @When("I send a GET request to retrieve the project object with ID {string}")
    public void i_send_a_get_request_to_retrieve_the_project_items_with_id(String id) {
        Response response = given()
                .when()
                .get("/projects/" + id);

        // Extract response body and status code
        responseBody = response.asString();
        responseStatus = response.getStatusCode();
    }

    @Then("the API should only return the details of the project item with ID {string}")
    public void the_api_should_only_return_the_details_of_the_project_item_with_id(String id) {
        // Check that the response status is 200 for successful retrieval
        assertEquals(200, responseStatus, "Expected status 200 for existing project item retrieval.");
        JsonPath jsonResponse = new JsonPath(responseBody);
        List<Object> project = jsonResponse.getList("projects");
        // Check that the size of the list is exactly 1
        assertEquals(1, project.size(), "Expected exactly one project item, but found " +  project.size());
    }
}
