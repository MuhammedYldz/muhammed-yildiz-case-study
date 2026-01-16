package com.insider.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.Reporter;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

/**
 * BasePage class contains common methods and wrappers for Selenium interactions.
 * I use this to abstract low-level Selenium commands and provide more readable and robust methods for Page Objects.
 */
public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public BasePage(WebDriver driver) {
        this.driver = driver;
        // I use Explicit Waits (WebDriverWait) because they are more reliable than Implicit Waits for dynamic elements.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    protected void click(By locator) {
        logger.info("Clicking element: {}", locator);
        Reporter.log("Clicking element: " + locator + "<br>");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (Exception e) {
            Reporter.log("Failed to click element: " + locator + "<br>");
            Assert.fail("Failed to click element: " + locator + ". Error: " + e.getMessage());
        }
    }

    protected void click(WebElement element) {
        try {
            Reporter.log("Clicking element: " + element + "<br>");
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            Reporter.log("Failed to click element. <br>");
            Assert.fail("Failed to click element. Error: " + e.getMessage());
        }
    }

    protected void type(By locator, String text) {
        logger.info("Typing '{}' into element: {}", text, locator);
        Reporter.log("Typing '" + text + "' into element: " + locator + "<br>");
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            Assert.fail("Failed to type into element: " + locator + ". Error: " + e.getMessage());
        }
    }

    protected WebElement find(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
             Assert.fail("Failed to find visible element: " + locator + ". Error: " + e.getMessage());
            return null;
        }
    }
    
    protected List<WebElement> findAll(By locator) {
        // Just waiting for presence, not necessarily visibility of all
        try {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (Exception e) {
              Assert.fail("Failed to find elements: " + locator + ". Error: " + e.getMessage());
             return null;
        }
    }

    protected String getText(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
        } catch (Exception e) {
             Assert.fail("Failed to get text from element: " + locator + ". Error: " + e.getMessage());
            return "";
        }
    }
    
    protected boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper to scroll into view
    protected void scrollToElement(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }
    
    // Helper to handle Javascript click if normal click is intercepted
    protected void jsClick(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
    
    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void switchToNewTab(String originalWindow) {
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                return;
            }
        }
        throw new RuntimeException("Could not switch to new tab");
    }

    protected void closeTabAndSwitchBack(String originalWindow) {
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    protected void selectByVisibleText(By locator, String text) {
        Reporter.log("Selecting '" + text + "' from: " + locator + "<br>");
        try {
            WebElement dropdown = find(locator);
            Select select = new org.openqa.selenium.support.ui.Select(dropdown);
            select.selectByVisibleText(text);
        } catch (Exception e) {
             Assert.fail("Failed to select '" + text + "' from: " + locator + ". Error: " + e.getMessage());
        }
    }
}
