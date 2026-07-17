@component @core @auth
Feature: Login
  Trainees and trainers authenticate with a username and password to receive a JWT.

  Scenario: Valid credentials return a token
    When the default user logs in with valid credentials
    Then the response status is 200
    And the response contains a "token" field

  Scenario: Unknown username is rejected
    When a user logs in with an unknown username
    Then the response status is 401
    And the response error code is 2805

  Scenario: Wrong password is rejected
    When the default user logs in with a wrong password
    Then the response status is 401
    And the response error code is 2805
