Feature: Delete Project Item
  As a user, I want to delete a project item so that I can remove projects I no longer need to complete.

  # Normal flow
  Scenario: Delete a specific project item
    Given a project item exists for deleting
    When I send a DELETE request for the project item 
    Then the system should remove the project item
    And the response should confirm the deletion of the project item

  # Alternate flow
  Scenario: Alternate Flow - Delete project without id
    Given there is a project with title "Project Title"
    When I send a DELETE request to this project without specifying its id
    Then the project response code returned is 404
  
  # Alternate flow
  Scenario: Error Flow - Delete a project with non-existent id
    When I send a DELETE request to this project with id "-1"
    Then the project response code returned is 404
    And the API project response should return an error message