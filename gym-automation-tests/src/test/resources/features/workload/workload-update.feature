@component @workload @workload-update
Feature: Trainer workload update
  gym-core-service publishes ADD/DELETE workload events for every training
  change; this suite exercises the same public contract directly.

  Background:
    Given an authenticated gym user

  Scenario: Adding workload for a new trainer succeeds
    When a workload entry is added for a trainer with 60 minutes
    Then the response status is 200

  Scenario: Removing minutes lowers the recorded duration
    Given a workload entry is added for a trainer with 60 minutes
    When 20 minutes are removed from that trainer's workload
    Then the response status is 200

  Scenario: A trainer username is required
    When workload is updated without a trainer username
    Then the response status is 400
    And the response error code is 400

  # Same Http403ForbiddenEntryPoint default as gym-core-service (see
  # permissions.feature) - workload-service has no custom entry point either.
  Scenario: No token is rejected
    When workload is updated without a token
    Then the response status is 403
