Feature: Add a new to-do item
  # normal flow scenario
  Scenario: Adding a new to-do item
    Given I have a new to-do item with title "Buy groceries"
    When I send a POST request to the "/todos" endpoint with the new to-do item
    Then the response status code should be- 201
    And the response should confirm the creation of the to-do item
    And the to-do item should be saved with a unique ID
  # alternate flow
  Scenario: Attempting to add a new to-do item without a title
    Given I have a new to-do item with title ""
    When I send a POST request to the "/todos" endpoint with the new to-do item
    Then the response status code should be- 400
    And the response should return a validation error message
  # error flow
  Scenario: Attempting to add a new to-do item with a malformed JSON request
    Given I have a malformed JSON request body for a new to-do item
    When I send a POST request to the "/todos" endpoint with the malformed request
    Then the response status code should be- 400
    And the response should return a parsing error message