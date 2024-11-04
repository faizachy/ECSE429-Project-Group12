Feature: Retrieve All To-Do Items
  As a user, I want to retrieve a list of all my to-do items so that I can review what tasks I need to complete.
# normal flow scenario
  Scenario: Retrieve list of all to-do items
    Given there are multiple to-do items in the system
    When I send a GET request to retrieve all to-do items
    Then the API should return a list of all items with their details
    And each to-do item in the list should display its ID, title, and completion status

# alternate flow
  Scenario: Retrieve a specific to-do item by ID
    Given there is a to-do item with ID "1" in the system
    When I send a GET request to retrieve the to-do object with ID "1"
    Then the API should only return the details of the to-do item with ID "1"
