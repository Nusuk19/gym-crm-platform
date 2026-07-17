# automation-tests

Component and integration BDD tests (Cucumber) for `gym-core-service` and `workload-service`.

Scenarios talk to both services purely over their public REST contracts, exactly like any
other real caller. There is **no Testcontainers and no in-process Spring context** in this
module - it has zero compile dependency on `gym-core-service` or `workload-service`. It assumes
the services you want to test are **already running** somewhere reachable (locally, a
docker-compose stack, CI), and simply points HTTP requests at them.

## Prerequisites

Start the services the same way you always do (IDE run configuration, or
`mvn spring-boot:run -pl gym-core-service` / `-pl workload-service`) and wait for
`Started GymCrmApplication` / `Started WorkloadApplication` in their logs before running these
tests. Their usual dependencies (PostgreSQL, MongoDB, Redis, ActiveMQ) must also be up.

Default addresses (see [Configuration](#configuration) to change them):

- `gym-core-service` at `http://localhost:8081`
- `workload-service` at `http://localhost:8082/workload-service`

## Running the tests

These tests are **off by default** (`skipAutomationTests=true` in `pom.xml`) so a normal
`mvn install` on the whole reactor never fails because the services aren't running. They only
run when explicitly requested.

### From the console (Maven)

```bash
mvn test -pl automation-tests -DskipAutomationTests=false
```

### From the IDE

Right-click `com.gym.crm.automation.CucumberTest` → Run. It's a JUnit 5 Platform Suite that
discovers every `.feature` file under `src/test/resources/features`. Running it directly from
the IDE bypasses the Surefire `skipAutomationTests` gate, so no extra flag is needed there.

### Running a subset

Every scenario is tagged. Filter with the standard Cucumber JUnit-platform property:

```bash
# only component-level scenarios (single service, no cross-service check)
mvn test -pl automation-tests -DskipAutomationTests=false -Dcucumber.filter.tags="@component"

# only integration scenarios (cross-service flow)
mvn test -pl automation-tests -DskipAutomationTests=false -Dcucumber.filter.tags="@integration"

# a single endpoint group
mvn test -pl gym-automation-tests -DskipAutomationTests=false "-Dcucumber.filter.tags=@auth"
mvn test -pl gym-automation-tests -DskipAutomationTests=false "-Dcucumber.filter.tags=@trainee-register"
mvn test -pl gym-automation-tests -DskipAutomationTests=false "-Dcucumber.filter.tags=@trainer-register"
mvn test -pl gym-automation-tests -DskipAutomationTests=false "-Dcucumber.filter.tags=@training-create"
mvn test -pl gym-automation-tests -DskipAutomationTests=false "-Dcucumber.filter.tags=@permissions"
mvn test -pl gym-automation-tests -DskipAutomationTests=false "-Dcucumber.filter.tags=@workload-update"
mvn test -pl gym-automation-tests -DskipAutomationTests=false "-Dcucumber.filter.tags=@workload-get"
mvn test -pl gym-automation-tests -DskipAutomationTests=false "-Dcucumber.filter.tags=@workload-queue"
```

## Configuration

Defaults live in
[`src/test/resources/automation-test.yml`](src/test/resources/automation-test.yml) - nothing is
hardcoded in Java. The nested YAML is flattened into dot-notation keys at load time (e.g.
`system.tests.core.base-url`), which is also the exact name of the system property that
overrides it.

**Edit the file** if the new value should become the checked-in default for everyone:

```yaml
system:
  tests:
    core:
      base-url: http://localhost:8081
    workload:
      base-url: http://localhost:8082/workload-service
```

**Pass a system property** for a one-off run against a different environment - always wins over
the file, no code changes needed:

```bash
mvn test -pl automation-tests -DskipAutomationTests=false \
  -Dsystem.tests.core.base-url=http://staging-host:8081 \
  -Dsystem.tests.workload.base-url=http://staging-host:8082/workload-service
```

A key missing from both the file and the system properties fails fast with a clear message,
rather than silently falling back to a guess.

## Module layout

| Package               | Responsibility                                                                  |
|------------------------|----------------------------------------------------------------------------------|
| `config`               | `TestProperties` - resolves base URLs / credentials (system property > YAML)    |
| `client`               | `ApiClient` (RestAssured), `WorkloadQueuePublisher` (raw JMS publish)            |
| `support`              | `TestContext` (per-scenario state), `DefaultUser` (session-wide bootstrapped user), `Payloads`, `Unique` |
| `hooks`                | `Hooks` - `@BeforeAll` bootstrap, registers the default user once per test session |
| `steps`                | Cucumber step definitions                                                       |
| `resources/features`   | Gherkin scenarios                                                                |
| `resources/automation-test.yml` | Default configuration                                                  |

Step classes only ever call `ApiClient`, never RestAssured directly, and never hardcode a base
URL - always go through `TestProperties`.

## Troubleshooting

- **`@workload-queue` scenario times out (10s) without the duration ever matching`** -
  the message likely never reached the listener, or was routed to
  `ActiveMQ.DLQ`. Check `workload-service`'s logs for `Invalid workload
  message, routing to DLQ` - and confirm the broker credentials in
  `automation-test.yml` (`system.tests.broker.*`) match what
  `workload-service` itself uses to connect (`spring.activemq.user/password`).
- **`gym-core-service` and `workload-service` must share the same `jwt.secret`** -
  the workload scenarios reuse the token obtained from `gym-core-service`'s
  `/auth/login`. If the two services are configured with different secrets
  (they are, by default, in `application-local.yml`), workload requests fail
  signature verification and every workload scenario returns `403` instead of
  the expected status. Set both to the same value before running
  `@workload-update` / `@workload-get`.
- **`Connection refused`** - the target service isn't running, or is on a different
  port/context-path than the defaults. Confirm it's up, or override the base URL as shown above.
- **`IllegalStateException: Missing automation-test property '...'`** - the key isn't set in
  `automation-test.yml` and wasn't passed as a system property either.
- **`UndefinedStepException` on every step** - almost always a mismatch between the
  `GLUE_PROPERTY_NAME` value in `CucumberTest.java` and the actual package of the step classes.
  Both must be exactly `com.gym.crm.automation.steps`.