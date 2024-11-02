Scenario: User adds a new to-do item
Given I have a new to-do item with title "Buy groceries"
When I send a POST request to the API to add the to-do item
Then the response should confirm the creation of the to-do item
And the to-do item should be saved