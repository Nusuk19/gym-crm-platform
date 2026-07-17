@integration @training-workload-flow
Feature: Training to workload flow
  gym-core-service publishes a workload update whenever a training is
  created; workload-service consumes it and reflects it in the trainer's
  monthly total. This is the one boundary these two services actually share,
  so it is the one flow tested end to end across both of them.

  Scenario: A created training increases the trainer's monthly workload
    Given an authenticated gym user
    And a registered trainee
    And a registered trainer
    When a training is created for the registered trainee and trainer
    Then the response status is 200
    And the trainer's monthly workload eventually reflects the created training duration

  Scenario: Deleting a trainee decreases the trainer's monthly workload back to zero
    Given an authenticated gym user
    And a registered trainee
    And a registered trainer
    When a training is created for the registered trainee and trainer
    Then the trainer's monthly workload eventually reflects the created training duration
    When the registered trainee deletes their own profile
    Then the response status is 200
    And the trainer's monthly workload eventually shows 0 minutes

  Scenario: An invalid workload message never updates a trainer's workload
    When an invalid workload message is published to the queue
    Then that message eventually appears on the dead letter queue with reason "trainingDuration must be positive"
