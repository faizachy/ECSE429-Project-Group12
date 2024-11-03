Feature: Delete To-Do Item
  As a user, I want to delete a to-do item so that I can remove tasks I no longer need to complete.

  Scenario: Delete a specific to-do item
    Given a to-do item exists with ID "123"
    When I send a DELETE request for the to-do item with ID "123"
    Then the system should remove the to-do item with ID "123"
    And the response should confirm the deletion of the to-do item
