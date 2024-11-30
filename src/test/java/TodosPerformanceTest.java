import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

    @TestMethodOrder(MethodOrderer.Random.class)
    public class TodosPerformanceTest {

        private final HttpClient client = HttpClient.newHttpClient();
        public static String categoryId = "0";
        public static String taskId = "0";
        public static String todoId = "0";
        @BeforeEach
        public void setup_foreach() throws IOException, InterruptedException {
            String requestBody = "{ \"title\": \"s aute irure dolor i\", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            String responseBody = response.body();
            todoId = new ObjectMapper().readTree(responseBody).get("id").asText();
            taskId = createTaskOfTodo();
            categoryId = createCategory();
        }

        @AfterEach
        public void teardown() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
        }
        @Test
        public void shouldRedirectToMainPage() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567"))
                    .GET().build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            assertEquals(302, response.statusCode(), "Expected redirect from main page");
        }
        public static String createTaskOfTodo() throws IOException, InterruptedException {
            // Initialize the HttpClient
            HttpClient client = HttpClient.newHttpClient();

            String requestBody = "{ \"Id\": \"1\" }";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/tasksof"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode());

            return "1";
        }

        public static String createCategory() throws IOException, InterruptedException {
            // Initialize the HttpClient
            HttpClient client = HttpClient.newHttpClient();
            String categoryRequestBody = "{ \"title\": \"Category Title\", \"description\": \"Category Description\" }";
            HttpRequest categoryRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/categories"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(categoryRequestBody))
                    .build();

            HttpResponse<String> categoryResponse = client.send(categoryRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, categoryResponse.statusCode());

            // Extract the category ID from the response body
            String responseBody = categoryResponse.body();
            String id = new ObjectMapper().readTree(responseBody).get("id").asText();

            String linkBody = "{ \"Id\": \"" + id + "\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" +todoId +"/categories"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(linkBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
            return id;
        }
//        @Test
//        public void testGetAllTodos() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos"))
//                    .GET().build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get all todos: " + (endTime - startTime) / 1_000_000 + " ms");
//            assertEquals(200, response.statusCode());
//
//        }

//        @Test
//        public void testHeadAllTodos() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos"))
//                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
//                    .build();
//
//            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get headers for all todos: " + (endTime - startTime) / 1_000_000 + " ms");
//            assertEquals(200, response.statusCode());
//        }

        @Test
        public void testCreateTodoWithInvalidDoneStatus() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"s aute irure dolor i\", \"doneStatus\": \"false\", \"description\": \"sse cillum dolore eu\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to create todo with invalid done status: " + (endTime - startTime) / 1_000_000 + " ms");
            // Check for expected error response
            assertEquals(400, response.statusCode());
        }

        @Test
        public void testCreateTodo() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"s aute irure dolor i\", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            // delete the created object
            String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
            HttpRequest request_delete = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                    .DELETE()
                    .build();

            HttpResponse<String> response_delete = client.send(request_delete, HttpResponse.BodyHandlers.ofString());
            // verify creation
            long endTime = System.nanoTime();
            System.out.println("Time taken to create todo: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(201, response.statusCode());
        }
        @Test
        public void testDeleteTodo() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            //create an object first
            String requestBody = "{ \"title\": \"s aute irure dolor i\", \"doneStatus\": false, \"description\": \"sse cillum dolore eu\" }";
            HttpRequest request_create = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response_create = client.send(request_create, HttpResponse.BodyHandlers.ofString());
            String responseBody = response_create.body();

            // delete and verify
            String todoId_delete = new ObjectMapper().readTree(responseBody).get("id").asText();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId_delete))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to delete todo: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(200, response.statusCode());
        }
//        @Test
//        public void testGetTodoById() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
//                    .GET().build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get todo by id: " + (endTime - startTime) / 1_000_000 + " ms");
//            assertEquals(200, response.statusCode());
//        }
//
//        @Test
//        public void testGetNonExistentTodo() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/-1"))
//                    .GET().build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get invalid todo: " + (endTime - startTime) / 1_000_000 + " ms");
//            assertEquals(404, response.statusCode());
//        }
//
//        @Test
//        public void testHeadTodoById() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
//                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
//                    .build();
//
//            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get headers by todo id: " + (endTime - startTime) / 1_000_000 + " ms");
//            assertEquals(200, response.statusCode());
//        }

        @Test
        public void testCreateTodoWithIncorrectId() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/-1"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("")) // Empty body
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to create todo with incorrect id: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(404, response.statusCode());
        }
        @Test
        public void testUpdateTodoWithID() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \" new title \", \"doneStatus\": false, \"description\": \"new description\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody)) // Empty body
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to update todo with id: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(200, response.statusCode());

        }

        @Test
        public void testUpdateTodoDoneStatusOnly() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"doneStatus\": true }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to update todo done status: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(400, response.statusCode());

        }

        @Test
        public void testUpdateTodoWithAllFields() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"Updated Title\", \"doneStatus\": false, \"description\": \"Updated Description\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to update todo: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(200, response.statusCode());

        }



        @Test
        public void testUpdateTodoTitle() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"New Title\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to update todo title: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(200, response.statusCode());

        }

        @Test
        public void testUpdateNonExistentTodo() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"title\": \"Title\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/-1"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to update nonexistent todo: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(404, response.statusCode());

        }

//        @Test
//        public void testGetCategoriesForTodo() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/1/categories"))
//                    .GET().build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get categories for todo: " + (endTime - startTime) / 1_000_000 + " ms");
//            assertEquals(200, response.statusCode());
//
//        }
//
//        @Test
//        public void testHeadCategoriesForTodo() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/1/categories"))
//                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
//                    .build();
//
//            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get headers for a todo: " + (endTime - startTime) / 1_000_000 + " ms");
//            assertEquals(200, response.statusCode());
//        }

        @Test
        public void testUpdateTodoWithMissingTitleFieldFails() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"doneStatus\": false }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to update todo with missing title fails: " + (endTime - startTime) / 1_000_000 + " ms");
            // Assert that the response is not successful (expecting an error)
            assertNotEquals(200, response.statusCode(), "Expected to fail when updating without title field.");

        }
        @Test
        public void testUpdateTodoWithMissingTitleFieldPasses() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"doneStatus\": false }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to update todo with missing title passes: " + (endTime - startTime) / 1_000_000 + " ms");
            assertEquals(400, response.statusCode(), "Expected to fail when updating without title field.");

        }
        @Test
        public void testCreateLinkBetweenTodoAndCategory() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"Id\": \"" + categoryId + "\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/categories"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to create link between todo and category: " + (endTime - startTime) / 1_000_000 + " ms");

            assertEquals(201, response.statusCode());

        }

        @Test
        public void testCreateLinkWithInvalidCategoryId() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"Id\": \"-1\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/categories"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to create link with invalid category: " + (endTime - startTime) / 1_000_000 + " ms");

            assertEquals(404, response.statusCode());

        }

        @Test
        public void testDeleteLinkBetweenTodoAndCategory() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/categories/" + categoryId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to delete link between todo and category: " + (endTime - startTime) / 1_000_000 + " ms");

            assertEquals(200, response.statusCode());

        }

        @Test
        public void testDeleteNonExistentLinkBetweenTodoAndCategory() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/categories/-1"))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to delete nonexistent link between todo and category: " + (endTime - startTime) / 1_000_000 + " ms");

            assertEquals(404, response.statusCode());

        }

//        @Test
//        public void testGetTasksOfTodo() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/tasksof"))
//                    .GET().build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get tasks of todo: " + (endTime - startTime) / 1_000_000 + " ms");
//
//            assertEquals(200, response.statusCode());
//
//        }
//
//        @Test
//        public void testGetTasksofInvalidTodoIdFails() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/-1/tasksof"))
//                    .header("Content-Type", "application/json")
//                    .GET()
//                    .build();
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get tasks of invalid todo fails: " + (endTime - startTime) / 1_000_000 + " ms");
//
//            // Assert that the response is not successful (expecting an error)
//            assertNotEquals(404, response.statusCode(), "Expected a 404 Not Found status code for invalid todo ID.");
//
//        }
//        @Test
//        public void testGetTasksofInvalidTodoIdPasses() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/-1/tasksof"))
//                    .header("Content-Type", "application/json")
//                    .GET()
//                    .build();
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get tasks of invalid todo passes: " + (endTime - startTime) / 1_000_000 + " ms");
//
//            // Assert that the response is not successful (expecting an error)
//            assertEquals(200, response.statusCode(), "Expected a 404 Not Found status code for invalid todo ID.");
//
//        }
//        @Test
//        public void testHeadTasksOfTodo() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "1/tasksof"))
//                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
//                    .build();
//
//            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get header of tasks of todo: " + (endTime - startTime) / 1_000_000 + " ms");
//
//            assertEquals(200, response.statusCode());
//        }

        @Test
        public void testCreateNewTaskOfTodo() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            String requestBody = "{ \"Id\": \"1\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/tasksof"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to create new task of todo: " + (endTime - startTime) / 1_000_000 + " ms");

            assertEquals(201, response.statusCode());

        }

        @Test
        public void testDeleteTaskOfTodo() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos/" + todoId + "/tasksof/1"))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to delete tasks of todo: " + (endTime - startTime) / 1_000_000 + " ms");

            assertEquals(200, response.statusCode());

        }



//        @Test
//        public void testGetTodosByDoneStatusTrue() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos?doneStatus=true"))
//                    .GET().build();
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get todos by done status true: " + (endTime - startTime) / 1_000_000 + " ms");
//
//            assertEquals(200, response.statusCode());
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode jsonResponse = objectMapper.readTree(response.body());
//            JsonNode todosArray = jsonResponse.get("todos");
//
//            assertTrue(todosArray.size() == 0);
//        }
//
//        @Test
//        public void testGetTodosByDoneStatusFalse() throws IOException, InterruptedException {
//            long startTime = System.nanoTime();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:4567/todos?doneStatus=false"))
//                    .GET().build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            long endTime = System.nanoTime();
//            System.out.println("Time taken to get todos by done status false: " + (endTime - startTime) / 1_000_000 + " ms");
//
//            assertEquals(200, response.statusCode());
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode jsonResponse = objectMapper.readTree(response.body());
//            JsonNode todosArray = jsonResponse.get("todos");
//
//            assertTrue(todosArray.size() > 0);
//        }
        @Test
        public void testMalformedJsonPayload() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            // Malformed JSON: missing a closing quote for the name value
            String malformedJson = "{ \"title\": \"Invalid Project, \"description\": \"This is malformed JSON\" }";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(malformedJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to test malformed json: " + (endTime - startTime) / 1_000_000 + " ms");

            // Assuming the server returns 400 for malformed JSON
            assertEquals(400, response.statusCode());
        }

        @Test
        public void testMalformedXmlPayload() throws IOException, InterruptedException {
            long startTime = System.nanoTime();
            // Malformed XML: missing a closing tag for <name>
            String malformedXml = "<project><title>Invalid Project<description>This is malformed XML</description></project>";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4567/todos"))
                    .header("Content-Type", "application/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(malformedXml))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.nanoTime();
            System.out.println("Time taken to test malformed xml: " + (endTime - startTime) / 1_000_000 + " ms");

            // Assuming the server returns 400 for malformed XML
            assertEquals(400, response.statusCode());
        }
    }

