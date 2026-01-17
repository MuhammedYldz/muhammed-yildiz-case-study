package com.insider.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class QAJobsPage extends BasePage {

    private final By locationFilter = By.id("filter-by-location");
    private final By departmentFilter = By.id("filter-by-department");
    private final By jobListContainer = By.id("jobs-list");
    private final By jobItem = By.className("position-list-item");
    
    private final By positionTitle = By.className("position-title");
    private final By positionDepartment = By.className("position-department");
    private final By positionLocation = By.className("position-location");
    
    private final By viewRoleBtn = By.cssSelector("a.btn");

    // Lever Page Selectors
    
    private final By leverLocation = By.cssSelector(".posting-category.location");
    private final By leverDepartment = By.cssSelector(".posting-category.department");

    public QAJobsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for the job list API request to complete and the job list to be populated.
     * This ensures that the page has fully loaded the initial set of jobs before we attempt to filter.
     * Uses a longer timeout to accommodate slow API responses.
     */
    public void waitForJobListApiLoad() {
        logger.info("Waiting for initial job list API response (extended timeout)...");
        
        // Use a longer wait specifically for this heavy loading operation
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(45));
        
        try {
            // Wait for the job list container to be present
            longWait.until(ExpectedConditions.presenceOfElementLocated(jobListContainer));
            
            // Scroll to the container to ensure any lazy-loading triggers are fired
            WebElement container = driver.findElement(jobListContainer);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", container);
            
            // Wait for at least one job item to appear, indicating the API has returned data
            longWait.until(d -> d.findElements(jobItem).size() > 0);
            logger.info("Initial job list loaded successfully.");
        } catch (org.openqa.selenium.TimeoutException e) {
            // If it times out, fail 
            Assert.fail("Timed out waiting for initial job list to load. The API might be slow or down.");
        }
    }

    public void filterJobs(String location, String department) {
        logger.info("Filtering jobs by Location: {} and Department: {}", location, department);
        
        // Wait until the location filter is populated
        wait.until(d -> {
            Select s = new Select(d.findElement(locationFilter));
            for (WebElement opt : s.getOptions()) {
                if (opt.getText().equals(location)) return true;
            }
            return false;
        });

        // Capture current state for staleness check
        List<WebElement> currentJobs = driver.findElements(jobItem);

        selectByVisibleText(locationFilter, location);
        selectByVisibleText(departmentFilter, department);
        
        // Wait for the job list to update
        // Wait for the old job items to become stale (removed from DOM)
        // This confirms that the filter application triggered a re-render.
        if (!currentJobs.isEmpty()) {
            logger.info("Waiting for job list to update (staleness check)...");
            try {
                wait.until(ExpectedConditions.stalenessOf(currentJobs.get(0)));
                logger.info("Old job list verified as stale. List is updating.");
            } catch (org.openqa.selenium.TimeoutException e) {
                logger.warn("Old job list did not become stale. The content might be identical or the update is very fast/slow.");
            }
        }
        
        // Then wait for the new list to be fully loaded
        waitForJobListApiLoad();
    }

    public void verifyJobListPresence() {
        logger.info("Verifying job list presence");
        Assert.assertTrue(isDisplayed(jobListContainer), "Job list container is not displayed");
        
        wait.until(d -> d.findElements(jobItem).size() > 0);
        
        List<WebElement> jobs = findAll(jobItem);
        Assert.assertTrue(jobs.size() > 0, "No jobs found in the list");
        logger.info("Found {} jobs", jobs.size());
    }

    public void verifyJobDetails() {
        // Wait for job list to update and contain elements that match the filter
        // We check ALL jobs to ensure the filter has been applied to the entire list, 
        
        try {
            wait.until(d -> {
                List<WebElement> jobs = d.findElements(jobItem);
                if (jobs.isEmpty()) return false;
                
                for (WebElement job : jobs) {
                    try {
                        // Get location and title for validation
                        WebElement locEl = job.findElement(positionLocation);
                        String locText = locEl.getText().trim();
                        if (locText.isEmpty()) locText = locEl.getAttribute("textContent").trim();
                        
                        WebElement titleEl = job.findElement(positionTitle);
                        String titleText = titleEl.getText().trim();
                        if (titleText.isEmpty()) titleText = titleEl.getAttribute("textContent").trim();

                        // Validation criteria matches verifySingleJobDetail logic
                        boolean isLocationMatch = locText.contains("Istanbul, Turkiye");
                        boolean isDepartmentMatch = titleText.contains("Quality Assurance") || titleText.contains("QA");

                        // If any job in the list doesn't match
                        if (!isLocationMatch || !isDepartmentMatch) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false; 
                    }
                }
                return true; // All visible jobs match the criteria
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            Assert.fail("Timeout waiting for job list to be fully filtered by Location (Istanbul) and Department (QA). Some jobs might not match.");
        }
        
        List<WebElement> jobs = findAll(jobItem);
        logger.info("Found {} filtered jobs. Verifying details for each...", jobs.size());
        
        if (jobs.isEmpty()) {
            Assert.fail("No jobs found after filtering!");
        }

        for (int i = 0; i < jobs.size(); i++) {
            verifySingleJobDetail(i);
        }
    }
    
    private void verifySingleJobDetail(int index) {
        // Retry mechanism for StaleElementReferenceException
        int attempts = 0;
        while (attempts < 3) {
            try {
                // Re-find elements to avoid StaleElementReferenceException
                List<WebElement> jobs = findAll(jobItem);
                if (index >= jobs.size()) return; // Should not happen given loop bounds
                
                WebElement currentJob = jobs.get(index);
                
                // Scroll to job
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", currentJob);
        
                WebElement titleEl = currentJob.findElement(positionTitle);
                WebElement deptEl = currentJob.findElement(positionDepartment);
                WebElement locEl = currentJob.findElement(positionLocation);
                
                String title = getElementText(titleEl);
                String department = getElementText(deptEl);
                String location = getElementText(locEl);
        
                logger.info("Checking Job: Title='{}', Dept='{}', Loc='{}'", title, department, location);
        
                Assert.assertTrue(title.contains("Quality Assurance") || title.contains("QA"), 
                        "Position title mismatch. Actual: " + title);
                Assert.assertTrue(department.contains("Quality Assurance") || department.contains("QA"), 
                        "Department mismatch. Actual: " + department);
                        
                // Strict check for "Istanbul, Turkiye"
                boolean isLocationMatch = location.contains("Istanbul, Turkiye");
                
                Assert.assertTrue(isLocationMatch, 
                        "Location mismatch. Actual: " + location + ". Expected to contain 'Istanbul, Turkiye'");
                
                return; // Success, exit loop
            } catch (StaleElementReferenceException e) {
                logger.warn("StaleElementReferenceException in verifySingleJobDetail (attempt {}). Retrying...", attempts + 1);
                attempts++;
            }
        }
        Assert.fail("Failed to verify job detail after 3 attempts due to StaleElementReferenceException");
    }
    
    private String getElementText(WebElement element) {
        String text = element.getText().trim();
        if (text.isEmpty()) {
            text = element.getAttribute("textContent").trim();
        }
        return text;
    }

    /**
     * Verifies that the 'View Role' button redirects to the correct Lever application page.
     * 
     * Strategy:
     * 1. Iterate through all jobs using an index loop to handle dynamic DOM updates.
     * 2. For each job:
     *    a. Re-find the job list to avoid StaleElementReferenceException.
     *    b. Verify the job still matches filters (Istanbul) to ensure page state hasn't reset.
     *    c. Scroll to the job and click 'View Role' (opens new tab).
     *    d. Switch to new tab, verify URL, close tab, switch back.
     *    e. Verify we returned to the correct state before proceeding.
     */
    public void clickAllViewRoleButtonsAndVerify() {
        wait.until(d -> d.findElements(jobItem).size() > 0);
        
        // Get initial count, but will re-query inside the loop
        int jobCount = findAll(jobItem).size();
        
        if (jobCount == 0) {
            Assert.fail("No jobs available to click View Role");
        }
        
        logger.info("Found {} jobs. Verifying 'View Role' links by clicking them one by one.", jobCount);

        String originalWindow = driver.getWindowHandle();
        
        for (int i = 0; i < jobCount; i++) {
            try {
                // Re-find elements to avoid StaleElementReferenceException
                List<WebElement> jobs = findAll(jobItem);
                
                // if the list size changed,  might go out of bounds or miss items
                if (i >= jobs.size()) {
                    logger.warn("Job list size changed during iteration. Stopping verification.");
                    break;
                }
                
                WebElement currentJob = jobs.get(i);
                
                // Scroll to job to ensure visibility
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", currentJob);
                
                // Optional: Re-verify this specific job matches filter before clicking
                    // This ensures we don't click a wrong job if filters reset
                    WebElement locEl = currentJob.findElement(positionLocation);
                    String locText = getElementText(locEl);
                    if (!locText.contains("Istanbul, Turkiye")) {
                         logger.warn("Filter reset detected! Found job with location: {}. Re-applying filters to continue verification.", locText);
                         // Re-apply filters
                         filterJobs("Istanbul, Turkiye", "Quality Assurance");
                         // Wait for list to update
                         waitForJobListApiLoad();
                         // Re-fetch the list
                         jobs = findAll(jobItem);
                         if (i >= jobs.size()) {
                             logger.error("Job list size changed after re-filtering. Cannot continue iteration safely.");
                             break;
                         }
                         currentJob = jobs.get(i);
                    }

                    WebElement viewBtn = currentJob.findElement(viewRoleBtn);
                String jobTitle = getElementText(currentJob.findElement(positionTitle));
                
                logger.info("Clicking 'View Role' for job #{}: {}", i + 1, jobTitle);
                
                // Click opens a new tab
                click(viewBtn);
                
                // Switch to new tab
                switchToNewTab(originalWindow);
                
                // Verify URL
                wait.until(d -> d.getCurrentUrl().contains("lever") || d.getCurrentUrl().contains("insider"));
                String currentUrl = driver.getCurrentUrl();
                Assert.assertTrue(currentUrl.contains("lever") || currentUrl.contains("jobs.lever.co"), 
                        "Redirected URL does not contain 'lever'. Actual: " + currentUrl);
                logger.info("Redirect verified: {}", currentUrl);
                
                // Verify Location and Department on Lever Page
                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(leverLocation));
                    
                    String actualLocation = driver.findElement(leverLocation).getText();
                    String actualDepartment = driver.findElement(leverDepartment).getText();
                    
                    logger.info("Lever Page - Location: {}, Department: {}", actualLocation, actualDepartment);
                    
                    Assert.assertTrue(actualLocation.contains("Istanbul, Turkiye"), 
                        "Lever Page Location mismatch. Actual: " + actualLocation);
                    Assert.assertTrue(actualDepartment.contains("Quality Assurance"), 
                        "Lever Page Department mismatch. Actual: " + actualDepartment);
                        
                } catch (Exception e) {
                     logger.error("Failed to verify Location/Department on Lever page: {}", e.getMessage());
                     
                     Assert.fail("Failed to verify Location/Department on Lever page: " + e.getMessage());
                }

                // Close tab and switch back
                closeTabAndSwitchBack(originalWindow);
                
                // Wait for the original page to be active and list to be present again
                wait.until(ExpectedConditions.presenceOfElementLocated(jobListContainer));
                
            } catch (Exception e) {
                logger.error("Failed to verify View Role for job index {}", i, e);
                Assert.fail("Failed to verify View Role for job index " + i + ": " + e.getMessage());
            }
        }
    }
    
    private void verifyViewRoleUrl(int index, String url, String originalWindow) {
        //  method, logic moved to clickAllViewRoleButtonsAndVerify
    }
}
