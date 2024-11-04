Feature: Retrieve a Specific To-Do Item
  As a user, I want to retrieve the details of a specific to-do item by its ID so that I can view the task's information individually.
  # normal flow scenario
  Scenario: Retrieve details of an existing to-do item
    Given a to-do item exists for getting with ID "2"
    When I send a GET request to retrieve the to-do item with ID "2"
    Then the API should return the details of only one to-do item
    And the ID for the item should be "2"
    And the response should include the following details:
      | title          | file paperwork    |
      | doneStatus     | false |
      | description    | |
  # alternate flow
  Scenario: Attempt to retrieve a non-existent to-do item
    Given there is no to-do item with ID "999"
    When I send a GET request to retrieve the to-do item with ID "999"
    Then the API should return an error message indicating that the to-do item does not exist
  # error flow
  Scenario: Attempt to retrieve a to-do item with a malformed request body
    When I send a malformed GET request with an invalid ID format
    Then the API should return an error