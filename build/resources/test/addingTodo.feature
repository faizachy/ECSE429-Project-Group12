Feature: Add a new to-do item

  Scenario: Adding a new to-do item
    Given I have a new to-do item with title "Buy groceries"
    When I send a POST request to the "/todos" endpoint with the new to-do item
    Then the response status code should be 201
    And the response should confirm the creation of the to-do item
    And the to-do item should be saved with a unique ID
