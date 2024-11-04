Feature: Mark To-Do as Completed
  As a user, I want to mark my to-do items as completed so that I can see which tasks are finished and which ones I still need to do.
  # normal flow scenario
  Scenario: Mark a to-do item as completed
    Given a to-do item exists with ID "1" and is not marked as completed
    When I send a PUT request to update the status to "true" for the to-do item with ID "1"
    Then the system should mark the to-do item with ID "1" as completed
    And the response should include a success message and display the updated completion status as "true"
  # alternate flow
  Scenario: Mark a completed to-do item as not completed
    Given a to-do item exists with ID "1" and is marked as completed
    When I send a PUT request to update the status to "false" for the to-do item with ID "1"
    Then the system should mark the to-do item with ID "1" as not completed
    And the response should include a success message and display the updated completion status as "false"
  # error flow
  Scenario: Attempt to update the completion status without specifying the status
    Given a to-do item exists with ID "1" and is not marked as completed
    When I send a PUT request to update the status for the to-do item with ID "1" with empty body
    Then the API should return a validation error message