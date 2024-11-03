Feature: Update Category

  Scenario: Normal Flow - Update title field for a specific category
    Given a category that has title "Category Old Title" exists
    When I send a request to update the title of category to "Category New Title"
    Then the response code that is returned is 200
    And the updated title will be "Category New Title"

  Scenario: Alternate Flow - Update title and description fields for a specific category
    Given a category that has title "Category Old Title" exists
    When I send a request to update the title of category to "Category New Title" and description "Category New Description"
    Then the response code that is returned is 200
    And the updated title will be "Category New Title" and description "Category New Description"

  Scenario: Error Flow - Update title field for non-existent category
    When I send a request to update the title of category with id "-1" to "Category New Title"
    Then the response code that is returned is 404