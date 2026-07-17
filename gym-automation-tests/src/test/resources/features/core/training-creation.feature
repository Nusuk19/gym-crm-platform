@component @core @training-create
Feature: Training creation

  Background:
    Given an authenticated gym user
    And a registered trainee
    And a registered trainer

  Scenario: A training is created successfully
    When a training is created for the registered trainee and trainer
    Then the response status is 200

  Scenario: An unknown trainee is rejected
    When a training is created for an unknown trainee
    Then the response status is 404
    And the response error code is 2835

  Scenario: A non-positive duration is rejected
    When a training is created with a non-positive duration
    Then the response status is 400
    And the response error code is 2760
