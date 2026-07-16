@component @smoke
Feature: Services are reachable
  As the automation-tests module
  I want to confirm both services are up before running domain scenarios
  So that a real failure never gets misread as "environment not started"

  Scenario: gym-core-service health check
    When I check the health of "core" service
    Then the service reports status "UP"

  Scenario: workload-service health check
    When I check the health of "workload" service
    Then the service reports status "UP"
