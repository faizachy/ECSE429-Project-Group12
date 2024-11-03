Feature: Retrieve All To-Do Items
  As a user, I want to retrieve a list of all my to-do items so that I can review what tasks I need to complete.

  Scenario: Retrieve list of all to-do items
    Given there are multiple to-do items in the system
    When I send a GET request to retrieve all to-do items
    Then the API should return a list of all items with their details
    And each to-do item in the list should display its ID, title, and completion status
