Feature: Retrieve a Specific Project Item
  As a user, I want to retrieve the details of a specific project item by its ID so that I can view the project's information individually.
  
  # normal flow 
  Scenario: Retrieve details of an existing project item
    Given a project item exists for ID "1"
    When I send a GET request to retrieve the project item with ID "1"
    Then the API should return the details of only one project item
    And the ID for the project item should be "1"
    And the project response should include the following details:
      | title          |  Office Work    |
      | completed      | false |
      | active         | false |
      | description    | |
  
  # alternate flow
  Scenario: Attempt to retrieve a non-existent project item
    Given there is no project item with ID "-1"
    When I send a GET request to retrieve the project item with ID "-1"
    Then the API should return an error message indicating that the project item does not exist
  
  # error flow
  Scenario: Attempt to retrieve a project item with a malformed request body
    When I send a malformed GET request for project with an invalid ID format
    Then the project API should return an error