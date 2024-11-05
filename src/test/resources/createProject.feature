Feature: Add a new project
  
  # normal flow scenario
  Scenario: Creating a new project
    Given I have a new project item with title "Garden Work"
    When I send a POST request to the "/projects" endpoint with the new project item
    Then the project response status code should be 201
    And the response should confirm the creation of the project item
    And the project item should be saved with a unique ID
  
  # alternate flow
  Scenario: Attempting to add a new project item with a new title and description
    Given I have a new project with title "NewProjectTitle" and description "NewProjectDescription"
    When I send a POST request to the "/projects" endpoint with the new project using title and description
    Then the project response status code should be 201
    And the response should confirm the creation of new project with title and description
  
  # error flow
  Scenario: Attempting to add a new project item with a malformed JSON request
    Given I have a malformed JSON request body for a new project item
    When I send a POST request to the "/projects" endpoint with the malformed project request
    Then the project response status code should be 400
    And the project response should return a parsing error message