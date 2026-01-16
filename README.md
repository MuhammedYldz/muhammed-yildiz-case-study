# Insider Quality Assurance Automation Case Study

This repository contains the test automation framework for the Insider Quality Assurance Case Study. The project uses **Java**, **Selenium WebDriver**, **TestNG**, and **Maven**.

## Prerequisites

- Java JDK 17 or higher
- Maven 3.8+
- Chrome Browser (Latest version)

## Project Structure

- **Page Object Model (POM)**: Located in `src/main/java/com/insider/pages`. Separates page locators and actions from test logic.
- **Tests**: Located in `src/test/java/com/insider/tests`. Contains the actual test scenarios.
- **Utilities**: `TestListener` handles failure screenshots and logging.

## Running Tests

You can run the tests using Maven from the command line.

**Note:** If `mvn` is not installed globally on your machine, you can use the local Maven included in this project:
`./apache-maven-3.9.6/bin/mvn`

### Run in Headless Mode (Recommended for CI/CD)
```bash
# Using global maven
mvn test -Dheadless=true

# OR using local maven
./apache-maven-3.9.6/bin/mvn test -Dheadless=true
```

### Run with UI (Visible Browser)
```bash
# Using global maven
mvn test -Dheadless=false

# OR using local maven
./apache-maven-3.9.6/bin/mvn test -Dheadless=false
```

## Test Reports

This project uses **Allure Framework** for detailed test reporting.

To generate and view the report after a test run:

```bash
mvn allure:serve
# OR
./apache-maven-3.9.6/bin/mvn allure:serve
```

## Implementation Details

- **Dynamic Waiting**: Uses Explicit Waits (`WebDriverWait`) for stability.
- **Robust Locators**: Handles dynamic elements and potential stale element exceptions.
- **Failure Analysis**: Automatically captures screenshots on test failure.
