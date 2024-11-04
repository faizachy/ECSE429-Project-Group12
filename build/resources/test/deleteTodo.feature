Feature: Delete To-Do Item
  As a user, I want to delete a to-do item so that I can remove tasks I no longer need to complete.

  # Normal flow
  Scenario: Delete a specific to-do item
    Given a to-do item exists for deleting
    When I send a DELETE request for the to-do item
    Then the system should remove the to-do item
    And the response should confirm the deletion of the to-do item

  # Alternate flow
  Scenario: Attempt to delete a non-existing to-do item
    Given no to-do item exists with ID "123"
    When I send a DELETE request for the to-do item with ID "123"
    Then the API should return an error indicating that the item could not be found

  # Error flow
  Scenario: Attempt to delete with a malformed ID
    When I send the malformed DELETE request with an invalid ID format
    Then the API response should return an error message
