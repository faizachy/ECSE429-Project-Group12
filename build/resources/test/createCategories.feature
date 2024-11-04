Feature: Create a New Category

  Scenario: Normal Flow - Creating a new category
    Given I have a new category with title "CategoryTest"
    When I send a POST request to the "/categories" endpoint with the new category using title
    Then the response status code should be 201
    And the response should confirm the creation of the category

  Scenario: Alternate Flow - Creating a new category
    Given I have a new category with title "NewCategoryTit" and description "descriptiontest"
    When I send a POST request to the "/categories" endpoint with the new category using title and description
    Then the response status code should be 201
    And the response should confirm the creation of new category with title and description

  Scenario: Error Flow - Creating a new category
    Given I attempt to create a new category with invalid title ""
    When I send a POST request to the "/categories" endpoint with the new category with invalid title
    Then the response status code should be 400



