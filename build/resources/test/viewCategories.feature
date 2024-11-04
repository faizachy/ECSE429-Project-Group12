Feature: View Categories Details

  Scenario: Normal Flow - View category details of category with title only
    Given that a category having ID 1 exists
    When I send a request to view the details of the category having ID 1
    Then the title of category with ID 1 retrieved is "Office"
    And the description of category with ID 1 retrieved is ""

  Scenario: Alternate Flow - View category details of category with title and description
    Given that a category having ID 4 exists
    When I send a request to view the details of the category having ID 4
    Then the title of category with ID 4 retrieved is "Category 4 Title"
    And the description of category with ID 4 retrieved is "Description Test"

  Scenario: Error Flow - View category details of category with non-existent id
    Given that a category having ID -1 does not exist
    When I send a request to view the details of the category having ID -1
    Then I should receive an error message indicating that the category with ID -1 wasn't found