@component @workload @workload-get
Feature: Trainer monthly workload retrieval

  Background:
    Given an authenticated gym user
    And a workload entry is added for a trainer with 90 minutes

  Scenario: Retrieving the recorded month returns the summed duration
    When that trainer's monthly workload is requested
    Then the response status is 200
    And the "trainingSummaryDuration" field equals 90

  Scenario: A month with no recorded training returns zero
    When that trainer's workload for a different month is requested
    Then the response status is 200
    And the "trainingSummaryDuration" field equals 0

  Scenario: An unknown trainer is rejected
    When an unknown trainer's monthly workload is requested
    Then the response status is 404
    And the response error code is 404

  Scenario: No token is rejected
    When that trainer's monthly workload is requested without a token
    Then the response status is 403
