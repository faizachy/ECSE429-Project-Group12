Feature: Retrieve All project Items
  As a user, I want to retrieve a list of all my project items so that I can review what projects I need to complete.

  # normal flow scenario
  Scenario: Retrieve list of all project items
    Given there are multiple project items in the system
    When I send a GET request to retrieve all project items
    Then the API should return a list of all project items with their details
    And each project item in the list should display its ID, title, description, completed and active

  # alternate flow
  Scenario: Retrieve a specific project item by ID
    Given there is a project item with ID "1" in the system
    When I send a GET request to retrieve the project object with ID "1"
    Then the API should only return the details of the project item with ID "1"