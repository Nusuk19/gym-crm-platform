@component @core @permissions
Feature: Trainee profile access control
  A trainee's profile is only visible to that trainee, never to another authenticated user.

  Background:
    Given an authenticated gym user
    And another trainee is registered

  Scenario: Another user's profile is denied
    When that other trainee's profile is requested with the current token
    Then the response status is 403
    And the response error code is 2806

  Scenario: No token is rejected
    When that other trainee's profile is requested without a token
    Then the response status is 403
