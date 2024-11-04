Feature: Delete a category

  Scenario: Normal Flow - Delete a specific category using id
    Given there is a category with title "Category Title"
    When I send a DELETE request to this category
    Then the response code returned is 200

  Scenario: Alternate Flow - Delete category without id
    Given there is a category with title "Category Title"
    When I send a DELETE request to this category without specifying its id
    Then the response code returned is 404

  Scenario: Error Flow - Delete a category with non-existent id
    When I send a DELETE request to this category with id "-1"
    Then the response code returned is 404