@component @workload @workload-queue
Feature: Trainer workload message consumption
  workload-service listens on trainer-workload-queue and persists what it
  receives. This suite publishes a raw message itself (standing in for
  gym-core-service) and checks the result purely through the public REST
  contract - no queue reads, so there is nothing to race against the real
  listener for.

  Background:
    Given an authenticated gym user

  Scenario: A published ADD message is reflected in the monthly workload
    When a raw workload message is published to the queue with 50 minutes
    Then that trainer's monthly workload eventually shows 50 minutes
