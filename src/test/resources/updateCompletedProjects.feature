Feature: Mark Project as Completed
  As a user, I want to mark my project items as completed so that I can see which projects are finished and which ones I still need to do.
  
  # normal flow
  Scenario: Mark a project item as completed
    Given a project item exists with ID "1" and is not marked as completed
    When I send a PUT request to update the status to "true" for the project item with ID "1"
    Then the system should mark the project item with ID "1" as completed
    And the project response should include a success message and display the updated completion status as "true"
  
  # alternate flow
  Scenario: Mark a completed project item as not completed
    Given a project item exists with ID "1" and is marked as completed
    When I send a PUT request to update the status to "false" for the project item with ID "1"
    Then the system should mark the project item with ID "1" as not completed
    And the project response should include a success message and display the updated completion status as "false"
  
  # error flow
  Scenario: Attempt to update the completion status without specifying the status
    Given a project item exists with ID "1" and is not marked as completed
    When I send a PUT request to update the completed field for the project item with ID "1" to null
    Then the project API should return a validation error message