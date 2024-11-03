Feature: Getting categories

  Scenario: Normal Flow - Get all categories
    When I send a GET request for all categories
    Then the response code should be 200
    And the response body should contain a list of categories

  Scenario: Alternate flow - Get a specific category
    Given a category exists with title "Work"
    When I send a GET request for category with id "1"
    Then the response code should be 200

  Scenario: Error Flow - Get a category with a non-existent id
    When I send a GET request for category with id "-1"
    Then the response code should be 404