Feature: Retrieve a Specific To-Do Item
  As a user, I want to retrieve the details of a specific to-do item by its ID so that I can view the task's information individually.

  Scenario: Retrieve details of an existing to-do item
    Given a to-do item exists with ID "123"
    When I send a GET request to retrieve the to-do item with ID "123"
    Then the API should return the details of the to-do item
    And the response should include the to-do item's ID, title, and completion status

  Scenario: Attempt to retrieve a non-existent to-do item
    Given there is no to-do item with ID "999"
    When I send a GET request to retrieve the to-do item with ID "999"
    Then the API should return an error message indicating that the to-do item does not exist
