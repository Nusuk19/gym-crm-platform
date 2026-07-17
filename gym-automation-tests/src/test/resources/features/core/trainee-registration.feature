@component @core @trainee-register
Feature: Trainee registration

  Scenario: A trainee registers successfully
    When a new trainee registers
    Then the response status is 200
    And the response contains a "username" field
    And the response contains a "password" field

  Scenario: Registering the same name twice yields a different username
    When a trainee registers, then registers again with the same name
    Then the response status is 200
    And the "username" field differs from "firstUsername"

  Scenario: A trainee registers with full profile details
    When a trainee registers with the following details:
      | firstName   | FlowTrainee |
      | lastName    | User        |
      | dateOfBirth | 2000-03-22  |
      | address     | 420 Oak St  |
    Then the response status is 200
    And the response contains a "username" field

  Scenario: A last name is required
    When a trainee registers without a last name
    Then the response status is 400
    And the response error code is 2760
