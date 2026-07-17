@component @core @trainer-register
Feature: Trainer registration

  Scenario: A trainer registers successfully
    When a new trainer registers with specialization "Cardio"
    Then the response status is 200
    And the response contains a "username" field

  Scenario: A trainer registers with full profile details
    When a trainer registers with the following details:
      | firstName      | FlowTrainer |
      | lastName       | User        |
      | specialization | Cardio      |
    Then the response status is 200
    And the response contains a "username" field

  Scenario: An unknown specialization is rejected
    When a new trainer registers with specialization "Underwater Basket Weaving"
    Then the response status is 404
    And the response error code is 2835

  Scenario: A first name is required
    When a trainer registers without a first name
    Then the response status is 400
    And the response error code is 2760
